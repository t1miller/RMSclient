package rr.rms.wifiaware

import android.content.Context
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import java.net.ServerSocket

object WifiAwareUtils {

    // Generate the configuration object necessary to publish a service,
    // or in our case, we're going to use a url like scheme
    // TODO: A fun idea in the future would be to send a JSON of
    // TODO: of websites we want and websites we have.
    fun generatePublishConfig(url: String): PublishConfig {
        return PublishConfig.Builder()
            .setServiceName(url)
            .setServiceSpecificInfo("android".toByteArray())
            .build()
    }

    // Generate the configuration object necessary to subscribe to a particular a service,
    // or in our case, we're going to use a url like scheme
    fun generateSubscribeConfig(url: String): SubscribeConfig {
        return SubscribeConfig.Builder()
            .setServiceName(url)
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

