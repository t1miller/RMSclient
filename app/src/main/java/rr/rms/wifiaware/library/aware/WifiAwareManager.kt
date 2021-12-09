package rr.rms.wifiaware.library.aware

import android.content.Context
import android.net.*
import android.net.wifi.aware.*
import android.net.wifi.aware.WifiAwareManager
import rr.rms.MainApplication
import rr.rms.wifiaware.library.logging.Logger
import timber.log.Timber
import java.net.ServerSocket
import java.net.Socket

/**
 * Singleton which manages wifi-aware interactions:
 * session, publish, subscribe, socket, send data, receive data
 */
object WifiAwareManager {

    interface SessionCallback {
        fun onSuccess(session: WifiAwareSession?)
        fun onError(msg: String)
    }

    interface SocketCallback {
        fun onResponse(socket: Socket?)
    }

    interface SubscribeCallback {
        fun onMessageSent(msgSent: String)
        fun onClientSocket(socket: Socket?)
        fun onError(msg: String)
    }

    interface PublishCallback {
        fun onMessageSent(msgSent: String)
        fun onServerSocket(socket: Socket?)
        fun onError(msg: String)
    }

    var session: WifiAwareSession? = null
    var publishSession: PublishDiscoverySession? = null
    var subscribeSession: SubscribeDiscoverySession? = null
    var publishPeerHandle: PeerHandle? = null
    var subscribePeerHandle: PeerHandle? = null
    lateinit var serverSocket: ServerSocket

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
                Timber.d("getSession() success")
                session = wifiAwareSession
                callback.onSuccess(session)
            }

            override fun onAttachFailed() {
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
     */
    fun publish(url: String, msg: String, callbackPublish: PublishCallback){
        getSession(object : SessionCallback {
            override fun onSuccess(session: WifiAwareSession?) {
                session?.publish(
                    WifiAwareUtils.buildPublishConfig(url),
                    object : DiscoverySessionCallback() {

                        override fun onSessionTerminated() {
                            Timber.e("publish session terminated")
                        }

                        override fun onPublishStarted(session: PublishDiscoverySession) {
                            Timber.d("Publishing started")
                            publishSession = session
                            Logger.log(Logger.ACTIONS.PUBLISH, Logger.me(), "everyone", "publishing")
                        }

                        override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                            Timber.d("Subscriber sent us Publishing url")
                            subscribePeerHandle = peerHandle
                            publishSession?.sendMessage(peerHandle, 0, msg.toByteArray())

                            getSocket(true, object: SocketCallback{
                                override fun onResponse(socket: Socket?) {
                                    callbackPublish.onServerSocket(socket)
                                }
                            })

                            Logger.log(Logger.ACTIONS.PUBLISH_MSG, Logger.me(), String(message), String(message))
                        }

                        override fun onMessageSendFailed(messageId: Int) {
                            Timber.d("message send failed")
                            callbackPublish.onError("message send failed")
                        }

                        override fun onMessageSendSucceeded(messageId: Int) {
                            Timber.d("message send succeeded")
                            Logger.log(Logger.ACTIONS.PUBLISH_MSG_MSG, Logger.me(), msg, msg)
                            callbackPublish.onMessageSent(msg)
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
    fun subscribe(url: String, urls: List<String>, msg: String, callbackSubscribe: SubscribeCallback?){
        getSession(object : SessionCallback {
            override fun onSuccess(session: WifiAwareSession?) {
                session?.subscribe(
                    WifiAwareUtils.buildSubscribeConfig(url, urls),
                    object : DiscoverySessionCallback() {

                        override fun onSessionTerminated() {
                            Timber.e("subscribe session terminated")
                        }

                        override fun onMessageSendFailed(messageId: Int) {
                            Timber.d("Message send failed")
                            callbackSubscribe?.onError("message send failed")
                        }

                        override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                            Timber.d("Subscriber sent msg: %s", String(message))
                            Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG_MSG, Logger.me(), String(message), String(message))

                            getSocket(false, object: SocketCallback{
                                override fun onResponse(socket: Socket?) {
                                    callbackSubscribe?.onClientSocket(socket)
                                }
                            })
                        }

                        override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                            Timber.d("Subscribe started")
                            subscribeSession = session
                            Logger.log(Logger.ACTIONS.SUBSCRIBE, Logger.me(),"everyone", "subscribing")
                        }

                        override fun onServiceDiscovered(
                            peerHandle: PeerHandle,
                            serviceSpecificInfo: ByteArray,
                            matchFilter: List<ByteArray>
                        ) {
                            publishPeerHandle = peerHandle
                            val srcId = String(serviceSpecificInfo)
                            Timber.d("Subscriber service discovered, service name = %s, sending msg = %s", srcId, msg)
                            subscribeSession?.sendMessage(peerHandle, 0, msg.toByteArray())
                            Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG, Logger.me(), srcId, msg)
                            callbackSubscribe?.onMessageSent(msg)
                        }
                    },
                    null
                )
            }

            override fun onError(msg: String) {
                Timber.d("error getting session")
            }
        })
        Timber.d("Subscribing to url: %s", url)
    }

    private fun getSocket(isServer: Boolean, socketCallback: SocketCallback) {
        Timber.d("setting up network socket")

        val connectManager = MainApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest: NetworkRequest? = if(isServer) {
            serverSocket = ServerSocket(0)
            Timber.d("server socket created, port ${serverSocket.localPort}")
            WifiAwareUtils.buildPublisherNetworkRequest(
                publishSession,
                subscribePeerHandle,
                serverSocket.localPort
            )
        } else {
            WifiAwareUtils.buildSubscriberNetworkRequest(subscribeSession, publishPeerHandle)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("socket onAvailable()")
            }

            override fun onUnavailable() {
                Timber.e("socket unavailable")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                Timber.e("socket losing()")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Timber.d("socket onCapabilitiesChanged()")

                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port

                Timber.d("socket: port used = $peerPort peerIpv6 = $peerIpv6 peerPort = ${peerAwareInfo.port}")

                if(isServer){
                    val socket = serverSocket.accept()
                    socketCallback.onResponse(socket)
                } else {
                    val socket = network.socketFactory.createSocket(peerIpv6, peerPort)
                    socketCallback.onResponse(socket)
                }
            }

            override fun onLost(network: Network) {
                Timber.e("socket onLost()")
            }
        }

        // todo when done with netowrk call unregisterNetworkCallback
        if (networkRequest != null){
            Timber.d("network request sent")
            connectManager.requestNetwork(networkRequest, callback)
        }
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