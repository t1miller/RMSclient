package rr.rms.utils

import ImageCache
import android.content.Context
import android.net.wifi.aware.*
import timber.log.Timber

/**
 *  Controls WifiAware interactions
 */
object WifiAwareController {

    var session: WifiAwareSession? = null

    init {
        Timber.d("WifiAwareController")
    }

    /**
     *  Interface to get subscribe callbacks
     */
    interface OnSubscribe {
        fun msgReceived(msg: ByteArray)
    }


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
    private fun getAwareSession(context: Context?): WifiAwareSession? {

        // We only want 1 active Wi-Fi aware session. If one
        // already exists then re-use it.
        if(session != null){
            return session
        }

        // No session exists, get a new one
        val wifiAwareManager = context?.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?

        wifiAwareManager?.attach(object : AttachCallback() {
            override fun onAttached(wifiAwareSession: WifiAwareSession?) {
                super.onAttached(wifiAwareSession)
                Timber.d("onAttach() WifiAwareManager succeed")
                session = wifiAwareSession
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                Timber.d("onAttachFailed() WifiAwareManager failed")
                session = null
            }
        }, null)
        return session
    }

    /**
     *  Close session when done
     */
    fun closeAwareSession() {
        session?.close()
    }

    /**
     *  Broadcast the default image in our cache. If a subscriber sees
     *  this broadcast and sends a message. We respond with a bear image.
     */
    fun broadcast(context: Context?) {
        Timber.d("Broadcasting a bear at %s", ImageCache.DEFAULT_URL)
        val awareSession =  getAwareSession(context)
        var discoverySession : PublishDiscoverySession? = null
        awareSession?.publish(
            WifiAwareUtils.generatePublishConfig(ImageCache.DEFAULT_URL),
            object : DiscoverySessionCallback() {

                override fun onPublishStarted(session: PublishDiscoverySession) {
                    Timber.d("onPublishStarted()")
                    discoverySession = session
                }

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    Timber.d("Subscriber sent msg: %s \nSending them a bear", message)
                    // A subscriber sent us a message. Send them our
                    // default bear image which is stamped
                    val defaultImage = ImageCache.getDefaultImage()
                    discoverySession?.sendMessage(peerHandle,0, defaultImage)
                }

            },
            null
        )
    }

    /**
     *  Subscribe to ask for other peoples' bear images.
     */
    fun subscribe(context: Context?, callback: OnSubscribe) {
        Timber.d("subscribe() on %s", ImageCache.DEFAULT_URL)
        val awareSession : WifiAwareSession? =  getAwareSession(context)
        var subscribeSession : SubscribeDiscoverySession? = null
        awareSession?.subscribe(
            WifiAwareUtils.generateSubscribeConfig(ImageCache.DEFAULT_URL),
            object : DiscoverySessionCallback() {

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    Timber.d("Subscriber sent msg: %s \nSending them a bear", message)

                    // broadcaster sent us their bear
                    callback.msgReceived(message)
                }

                override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                    Timber.d("onSubscribeStarted()")
                    subscribeSession = session
                }

                override fun onServiceDiscovered(
                    peerHandle: PeerHandle,
                    serviceSpecificInfo: ByteArray,
                    matchFilter: List<ByteArray>
                ) {
                    Timber.d("onServiceDiscovered()\n")

                    // send message and broadcaster will respond with their stamped bear
                    val msg = "Blah".toByteArray()
                    subscribeSession?.sendMessage(peerHandle,0, msg)
                }

            },
            null
        )
    }

    fun toString(context: Context?) : String {
        var status = "status:"
        status += "\nisAvailable: " + WifiAwareUtils.isAvailable(context)
        return status
    }



}