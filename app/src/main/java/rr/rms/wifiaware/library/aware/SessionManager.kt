package rr.rms.wifiaware.library.aware

import android.content.Context
import android.net.wifi.aware.*
import android.net.wifi.aware.WifiAwareManager
import rr.rms.MainApplication
import rr.rms.wifiaware.library.logging.Logger
import timber.log.Timber


object SessionManager {

    private val wifiAwareManager = MainApplication.applicationContext.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
    private var mWifiAwareSession: WifiAwareSession? = null
    private lateinit var subscribeSession: SubscribeDiscoverySession
    private lateinit var publishSession: PublishDiscoverySession
//    private val scope = CoroutineScope(Job() + Dispatchers.IO)

//    private var publishSession: PublishDiscoverySession? = null
//    private var publishPeerHandle: PeerHandle? = null

    interface Session {
        fun onSession(session: WifiAwareSession)
    }

    fun getLatestSession(callback: Session?){
        if(mWifiAwareSession != null){
            callback?.onSession(mWifiAwareSession!!)
        } else {
            val sessionCallback = object : AttachCallback() {
                override fun onAttached(wifiAwareSession: WifiAwareSession?) {
                    Timber.d("getSession() success")
                    mWifiAwareSession = wifiAwareSession
                    Logger.log(Logger.ACTIONS.SESSION, "", "", "")
                    callback?.onSession(mWifiAwareSession!!)
                }

                override fun onAttachFailed() {
                    Timber.d("getSession() failed")
                }
            }
            // todo - the handler should nnot run on UI thread
            wifiAwareManager?.attach(sessionCallback, null)
        }
    }

    fun getSessionAndPublish(url: String, msg: String) {
        try {
            publish(url, msg)
        } catch (e: Exception){
            Timber.d("error publishing: $e")
        }
    }

    private fun publish(url: String, msg: String) {
        val config = WifiAwareUtils.buildPublishConfig(url)
        val callback = object : DiscoverySessionCallback() {

            override fun onPublishStarted(session: PublishDiscoverySession) {
                Timber.d("Publishing started")
                publishSession = session
                Logger.log(Logger.ACTIONS.PUBLISH, Logger.me(), "everyone", "publishing")
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                Timber.d("Subscriber sent us their url")
                // todo this is fired but,
                //      this send message isnt working
                // not neccessary to send message back, we cann directly set up a connection
                publishSession.sendMessage(peerHandle, 0, "hello sir".toByteArray())
                Logger.log(Logger.ACTIONS.PUBLISH_MSG, Logger.me(), String(message), String(message))
//                ServerClientManager.sendDataToClient(publishSession, peerHandle)
            }

            override fun onMessageSendSucceeded(messageId: Int) {
                Timber.d("message send succeeded")
                Logger.log(Logger.ACTIONS.PUBLISH_MSG_MSG, Logger.me(), msg, msg)
            }

            override fun onMessageSendFailed(messageId: Int) {
                Timber.d("message send failed")
            }

            override fun onSessionTerminated() {
                Timber.d("publish session terminated")
            }
        }
        // todo - this should not be on main UI thread
        mWifiAwareSession?.publish(config, callback, null)
    }

    fun getSessionAndSubscribe(url: String, urls: List<String>, msg: String) {
        try {
            subscribe(url, urls, msg)
        } catch (e: Exception){
            Timber.e("error subscribing: $e")
        }
    }

    private fun subscribe(url: String, urls: List<String>, msg: String) {
        Timber.d("subscribing")
        val config = WifiAwareUtils.buildSubscribeConfig(url, urls)
        val callback = object : DiscoverySessionCallback() {
            override fun onSessionTerminated() {
                Timber.e("subscribe session terminated")
            }

            override fun onMessageSendFailed(messageId: Int) {
                Timber.d("Message send failed")
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // todo
                //  this isnt getting fired
                //
                Timber.d("Publisher sent msg: %s", String(message))
                Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG_MSG, Logger.me(), String(message), String(message))
////                publishPeerHandle = peerHandle
                // todo shouldnt this use peerhandle from arg?
//                scope.launch {
//                    ServerClientManager.receiveDataFromServer(subscribeSession, peerHandle, scope)
//                }
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
//                publishPeerHandle = peerHandle
                val srcId = String(serviceSpecificInfo)
                Timber.d("Publisher service discovered, service name = %s, sending msg = %s", srcId, msg)
                subscribeSession.sendMessage(peerHandle, 0, "hello".toByteArray())
                Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG, Logger.me(), srcId, msg)
            }
        }
        mWifiAwareSession?.subscribe(config, callback, null)
    }

    fun close() {
        mWifiAwareSession?.close()
    }
}
