package rr.rms.wifiaware.library

import android.content.Context
import android.net.*
import android.net.wifi.aware.*
import rr.rms.MainApplication
import timber.log.Timber
import java.net.Socket

/**
 * Singleton which manages wifi-aware interactions:
 * session, publish, subscribe, socket
 */
object WifiAwareClient {

    interface SubscribeCallback {
        fun onMessageReceived(subscribeDiscoverySession: SubscribeDiscoverySession?, peerHandle: PeerHandle?, msgRcvd: ByteArray)
        fun onMessageSent(msgSent: String)
        fun onError(msg: String)
    }

    interface PublishCallback {
        fun onMessageReceived(publishDiscoverySession: PublishDiscoverySession?, peerHandle: PeerHandle?, msgRcvd: ByteArray)
        fun onMessageSent(msgSent: String)
        fun onError(msg: String)
    }

    interface SessionCallback {
        fun onSuccess(session: WifiAwareSession?)
        fun onError(msg: String)
    }

    interface SocketCallback {
        fun onResponse(socket: Socket?)
    }

    var session: WifiAwareSession? = null
    var publishSession: PublishDiscoverySession? = null
    var subscribeSession: SubscribeDiscoverySession? = null

    /**
     * Get an aware session
     *
     * - turns on the Wi-Fi Aware hardware
     * - joins or forms a Wi-Fi Aware cluster
     * - creates a Wi-Fi Aware session with a unique namespace that acts
     *   as a container for all discovery sessions created within it.
     *
     * @return a WifiAwareSession or null
     */
    private fun getSession(callback: SessionCallback) {
        Timber.d("getSession()")

        if(session != null){
            // We only want 1 active Wi-Fi aware session.
            Timber.d("session already exists")
            callback.onSuccess(session)
            return
        }

        Timber.d("no existing session, creating new session")

        val wifiAwareManager = MainApplication.applicationContext.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        wifiAwareManager?.attach(object : AttachCallback() {
            override fun onAttached(wifiAwareSession: WifiAwareSession?) {
                super.onAttached(wifiAwareSession)
                Timber.d("getSession() success")
                session = wifiAwareSession
                callback.onSuccess(session)
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                Timber.d("getSession() failed")
                session = null
                callback.onError("onAttachFailed() WifiAwareManager failed")
            }
        }, null)

    }

    /**
     * Publish to a url service
     * @param url The url to publish
     * @param msg The message sent to the subscriber if they send us a message
     * @param callback To no
     */
    fun publish(url: String, msg: String, callback: PublishCallback){
        getSession(object : SessionCallback {
            override fun onSuccess(session: WifiAwareSession?) {
                session?.publish(
                    WifiAwareUtils.generatePublishConfig(url),
                    object : DiscoverySessionCallback() {
                        override fun onPublishStarted(session: PublishDiscoverySession) {
                            Timber.d("Publishing started")
                            publishSession = session
                        }

                        override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                            Timber.d("Subscriber sent us Publishing url")
                            publishSession?.sendMessage(peerHandle, 0, msg.toByteArray())
                            callback.onMessageReceived(publishSession, peerHandle, message)
                        }

                        override fun onMessageSendFailed(messageId: Int) {
                            Timber.d("message send failed")
                            callback.onError("message send failed")
                        }

                        override fun onMessageSendSucceeded(messageId: Int) {
                            Timber.d("message send succeeded")
                            callback.onMessageSent(msg)
                        }
                    },
                    null
                )
            }

            override fun onError(msg: String) {
                Timber.d("error getting session")
            }
        })
        Timber.d("Publishing a url: %s", url)
    }

    /**
     * Subscribe to a url service
     * @param url The url subscribing to
     * @param callback WifiAwareCallback to notify user of events
     */
    fun subscribe(url: String, urls: List<String>, msg: String, callback: SubscribeCallback){
        Timber.d("Subscribing to url: %s", url)
        getSession(object : SessionCallback {
            override fun onSuccess(session: WifiAwareSession?) {
                session?.subscribe(
                    WifiAwareUtils.generateSubscribeConfig(url, urls),
                    object : DiscoverySessionCallback() {
                        override fun onMessageSendFailed(messageId: Int) {
                            Timber.d("Message send failed")
                            callback.onError("message send failed")
                        }

                        override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                            Timber.d("Subscriber sent msg: %s", String(message))
                            callback.onMessageReceived(subscribeSession, peerHandle, message)
                        }

                        override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                            Timber.d("Subscribe started")
                            subscribeSession = session
                        }

                        override fun onServiceDiscovered(
                            peerHandle: PeerHandle,
                            serviceSpecificInfo: ByteArray,
                            matchFilter: List<ByteArray>
                        ) {
                            val srcId = String(serviceSpecificInfo)
                            Timber.d("Subscriber service discovered, service name = %s, sending msg = %s", srcId, msg)
                            subscribeSession?.sendMessage(peerHandle, 0, msg.toByteArray())
                            callback.onMessageSent(msg)
                        }
                    },
                    null
                )
            }

            override fun onError(msg: String) {
                Timber.d("error getting session")
            }
        })
    }

    fun getNetworkSocket(context: Context, isPublisher: Boolean, discoverySession: DiscoverySession?, peerHandle: PeerHandle?, socketResponse: SocketCallback) {
        Timber.d("setting up network socket")

        val connectManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = buildWifiAwareNetworkRequest(isPublisher, discoverySession, peerHandle)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("network onAvailable()")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Timber.d("network onCapabilitiesChanged()")
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port
                val socket = network.socketFactory.createSocket(peerIpv6, peerPort)
                socketResponse.onResponse(socket)
            }

            override fun onLost(network: Network) {
                Timber.d("network onLost()")
                socketResponse.onResponse(null)
            }
        }

        // todo when done with netowrk call unregisterNetworkCallback
        if(networkRequest != null){
            connectManager.requestNetwork(networkRequest, callback)
        }
    }

    private fun buildWifiAwareNetworkRequest(isPublisher: Boolean, discoverySession: DiscoverySession?, peerHandle: PeerHandle?) : NetworkRequest?{
        if(discoverySession == null){
            Timber.e("cant build network request, discovery session null")
            return null
        }
        if(peerHandle == null){
            Timber.e("cant build network request, peer handle null")
            return null
        }
        val networkRequest = if(isPublisher) buildPublisherNetworkRequest(discoverySession, peerHandle) else buildSubscriberNetworkRequest(discoverySession, peerHandle)
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkRequest)
            .build()
    }

    private fun buildPublisherNetworkRequest(discoverySession: DiscoverySession, peerHandle: PeerHandle): NetworkSpecifier {
        return WifiAwareNetworkSpecifier.Builder(discoverySession, peerHandle)
                .setPskPassphrase("somePassword")
                .setPort(NetworkUtils.getAPort())
                .build()
    }

    private fun buildSubscriberNetworkRequest(discoverySession: DiscoverySession, peerHandle: PeerHandle): NetworkSpecifier {
        return WifiAwareNetworkSpecifier.Builder(discoverySession, peerHandle)
            .setPskPassphrase("somePassword")
            .build()
    }

    /**
     *  Close session when done
     */
    fun closeAwareSession() {
        subscribeSession?.close()
        publishSession?.close()
        session?.close()
    }
}