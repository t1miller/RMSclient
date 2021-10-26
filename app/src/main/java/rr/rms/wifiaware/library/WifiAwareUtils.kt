package rr.rms.wifiaware.library

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.aware.*
import android.provider.Settings
import rr.rms.MainApplication

object WifiAwareUtils {

    /**
     * Generate a publish config
     * @param serviceName The service name to subscribe to
     * @return A PublishConfig
     */
    fun generatePublishConfig(serviceName: String): PublishConfig {
        return PublishConfig.Builder()
            .setServiceName(serviceName)
            .setServiceSpecificInfo(getDeviceId().toByteArray())
            .build()
    }

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
            .setServiceSpecificInfo(getDeviceId().toByteArray())
            .build()
    }

    /**
     * Checks if WiFi Aware is available on this device
     * @return true if available else false
     */
    fun isAvailable(context: Context?): Boolean {
        if (context == null) return false
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        return wifiAwareManager.isAvailable
    }

    /**
     *  Quick way to get a unique identifier for each device
     *
     *  todo this method has issues
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(MainApplication.applicationContext.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }
}

