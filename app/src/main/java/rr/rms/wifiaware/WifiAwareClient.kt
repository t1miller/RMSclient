package rr.rms.wifiaware

import android.content.Context
import android.net.wifi.aware.*
import timber.log.Timber

// Todo need some data structure that gives insight into the current state of Wifi-Aware
/**
 * Singleton which manages wifi-aware interactions
 */
object WifiAwareClient {

    interface WifiAwareCallback {
        fun onSuccess(bytes: ByteArray)
        fun onError(msg: String)
    }

    interface WifiAwareSessionCallback {
        fun onSuccess(session: WifiAwareSession?)
        fun onError(msg: String)
    }

//    /**
//     * States that wifi-aware client can be in
//     */
//    enum class State {
//        START,
//        GETTING_WIFI_AWARE_SESSION,
//        GETTING_PUBLISH_SESSION,
//        GETTING_SUBSCRIBE_SESSION,
//        ERROR
//    }

    var session: WifiAwareSession? = null

    var publishSession: PublishDiscoverySession? = null

    var subscribeSession: SubscribeDiscoverySession? = null

    var mPeerHandle: PeerHandle? = null

//    var currentState: State = State.START

//    /**
//     * State machine which does setup
//     */
//    fun setupStateMachine(context: Context?, state: State) {
//        Timber.d("state machine: state = %s", state.name)
//        currentState = state
//        when(state) {
//            State.START -> {
//                setupStateMachine(context, State.GETTING_WIFI_AWARE_SESSION)
//            }
//            State.GETTING_WIFI_AWARE_SESSION -> {
//                session = getSession(context)
//                if (session != null){
//                    setupStateMachine(context, State.GETTING_PUBLISH_SESSION)
//                } else {
//                    setupStateMachine(context, State.GETTING_WIFI_AWARE_SESSION)
//                }
//            }
//            State.GETTING_PUBLISH_SESSION -> {
//                publish()
//            }
//            State.GETTING_SUBSCRIBE_SESSION -> {
//
//            }
//            State.ERROR -> {
//
//            }
//        }
//    }

    /**
     * Get an aware session
     *
     * - turns on the Wi-Fi Aware hardware
     * - joins or forms a Wi-Fi Aware cluster
     * - creates a Wi-Fi Aware session with a unique namespace that acts
     *   as a container for all discovery sessions created within it.
     *
     * @param context The context
     * @return a WifiAwareSession or null
     */
    private fun getSession(context: Context?, callback: WifiAwareSessionCallback) {
        Timber.d("getSession()")

        if(session != null){
            // We only want 1 active Wi-Fi aware session.
            Timber.d("session already exists")
            callback.onSuccess(session)
            return
        }

        Timber.d("no existing session, creating new session")
        val wifiAwareManager = context?.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        wifiAwareManager?.attach(object : AttachCallback() {
            override fun onAttached(wifiAwareSession: WifiAwareSession?) {
                super.onAttached(wifiAwareSession)
                Timber.d("onAttach() WifiAwareManager succeed")
                session = wifiAwareSession
                callback.onSuccess(session)
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                Timber.d("onAttachFailed() WifiAwareManager failed")
                session = null
                callback.onError("onAttachFailed() WifiAwareManager failed")
            }
        }, null)
    }

    /**
     * Publish to a url service
     * @param context Context
     * @param url The url to publish
     * @param callback To no
     */
    fun publish(context: Context?, url: String, callback: WifiAwareCallback, msg: String){
        getSession(context, object : WifiAwareSessionCallback {
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
                                mPeerHandle = peerHandle
                                publishSession?.sendMessage(peerHandle, 0, msg.toByteArray())
                            }

                            override fun onMessageSendFailed(messageId: Int) {
                                Timber.d("message send failed")
                                callback.onError("message send failed")
                            }

                            override fun onMessageSendSucceeded(messageId: Int) {
                                Timber.d("message send succeeded")
                                callback.onSuccess(msg.toByteArray())
                            }
                        },
                        null
                    )
                }

                override fun onError(msg: String) {
                    Timber.d("error getting session")
                }
            }
        )
        Timber.d("Publishing a url: %s", url)

    }

    /**
     * Subscribe to a url service
     * @param context Context
     * @param url The url subscribing to
     * @param callback WifiAwareCallback to notify user of events
     */
    fun subscribe(context: Context?, url: String, urls: List<String>, callback: WifiAwareCallback){
        Timber.d("Subscribing to url: %s", url)
        getSession(context, object : WifiAwareSessionCallback {
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
                            mPeerHandle = peerHandle
                            callback.onSuccess(message)
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
                            val serviceName = String(serviceSpecificInfo)
                            val msgToSend = "send me data"
                            Timber.d("Subscriber service discovered, service name = %s, sending msg = %s", serviceName, msgToSend)

                            subscribeSession?.sendMessage(peerHandle, 0, msgToSend.toByteArray())
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

    /**
     *  Close session when done
     */
    fun closeAwareSession() {
        subscribeSession?.close()
        publishSession?.close()
        session?.close()
    }
}