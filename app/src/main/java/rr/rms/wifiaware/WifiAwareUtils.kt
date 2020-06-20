package rr.rms.wifiaware

import android.content.Context
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import java.net.ServerSocket

object WifiAwareUtils {

    /**
     * Generate a publish config
     * @param serviceName The service name to subscribe to
     * @return A PublishConfig
     */
    fun generatePublishConfig(serviceName: String): PublishConfig {
        return PublishConfig.Builder()
            .setServiceName(serviceName)
            .setServiceSpecificInfo("android".toByteArray())
            .build()
    }

//    /**
//     * Generate a subscribe config
//     * @param serviceName The service name to subscribe to
//     * @param filter An optional list of other services to also subscribe to
//     * @return A SubscribeConfig
//     */
//    fun generateSubscribeConfig(serviceName: String): SubscribeConfig {
//        return SubscribeConfig.Builder()
//            .setServiceName(serviceName)
//            .setServiceSpecificInfo("android".toByteArray())
//            .build()
//    }
//
    /**
     * Generate a subscribe config
     * @param serviceName The service name to subscribe to
     * @param filter An optional list of other services to also subscribe to
     * @return A SubscribeConfig
     */
    fun generateSubscribeConfig(serviceName: String, filter: List<String>): SubscribeConfig {
        return SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .setMatchFilter(filter.map {it.toByteArray()})
            .setServiceSpecificInfo("android".toByteArray())
            .build()
    }

    fun isAvailable(context: Context?): Boolean {
        if (context == null) return false
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        return wifiAwareManager.isAvailable
    }

    fun getAPort() : Int {
        val ss = ServerSocket(0)
        return ss.localPort
    }
}

