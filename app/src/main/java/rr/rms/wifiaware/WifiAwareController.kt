package rr.rms.wifiaware

import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
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
    fun getSession(context: Context?): WifiAwareSession? {
        Timber.d("getSession()")

        // We only want 1 active Wi-Fi aware session.
        if(session != null){
            Timber.d("session already exists")
            return session
        }

        Timber.d("no existing session, creating new session")

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

}