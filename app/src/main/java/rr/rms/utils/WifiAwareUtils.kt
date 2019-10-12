package rr.rms.utils

import android.content.Context
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager

object WifiAwareUtils {

    // Generate the configuration object necessary to publish a service,
    // or in our case, we're going to use a url like scheme
    // TODO: A fun idea in the future would be to send a JSON of
    // TODO: of websites we want and websites we have.
    fun generatePublishConfig(url: String): PublishConfig {
        return PublishConfig.Builder().setServiceName(url).build()
    }

    // Generate the configuration object necessary to subscribe to a particular a service,
    // or in our case, we're going to use a url like scheme
    fun generateSubscribeConfig(url: String): SubscribeConfig {
        return SubscribeConfig.Builder().setServiceName(url).build()
    }

    fun isAvailable(context: Context?): Boolean {
        if (context == null) return false
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        return wifiAwareManager.isAvailable
    }

//    /**
//     * Generic discovery session template
//     */
//    fun buildDiscoverySessionCallback() : DiscoverySessionCallback {
//        return object : DiscoverySessionCallback() {
//            override fun onPublishStarted(session: PublishDiscoverySession) {
//                Timber.d("onPublishStarted()")
//            }
//
//            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
//                Timber.d("onMessageReceived()")
//            }
//
//            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
//                Timber.d("onSubscribeStarted()")
//            }
//
//            override fun onServiceDiscovered(
//                peerHandle: PeerHandle,
//                serviceSpecificInfo: ByteArray,
//                matchFilter: List<ByteArray>
//            ) {
//                Timber.d("onServiceDiscovered()")
//            }
//
//        }
//    }

}

