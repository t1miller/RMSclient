package rr.rms.wifiaware.library

import android.annotation.SuppressLint
import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.provider.Settings
import rr.rms.MainApplication
import timber.log.Timber

object WifiAwareUtils {

    fun buildPublisherNetworkRequest(discoverySession: DiscoverySession?, peerHandle: PeerHandle?, port: Int): NetworkRequest? {
        if(discoverySession == null){
            Timber.e("cant build network request, discovery session null")
            return null
        }
        if(peerHandle == null){
            Timber.e("cant build network request, peer handle null")
            return null
        }
        val networkRequest = WifiAwareNetworkSpecifier.Builder(discoverySession, peerHandle)
            .setPskPassphrase("superSuperSecret")
            .setPort(port)
            .build()
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkRequest)
            .build()

    }

    fun buildSubscriberNetworkRequest(discoverySession: DiscoverySession?, peerHandle: PeerHandle?): NetworkRequest? {
        if(discoverySession == null){
            Timber.e("cant build network request, discovery session null")
            return null
        }
        if(peerHandle == null){
            Timber.e("cant build network request, peer handle null")
            return null
        }
        val networkRequest =  WifiAwareNetworkSpecifier.Builder(discoverySession, peerHandle)
            .setPskPassphrase("superSuperSecret")
            .build()
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkRequest)
            .build()
    }

    /**
     * Generate a publish config
     * @param serviceName The service name to subscribe to
     * @return A PublishConfig
     */
    fun buildPublishConfig(serviceName: String): PublishConfig {
        return PublishConfig.Builder()
            .setServiceName(serviceName)
            .setServiceSpecificInfo(getDeviceId().toByteArray())
            .setPublishType(PublishConfig.PUBLISH_TYPE_UNSOLICITED)
            .setTerminateNotificationEnabled(true)
            .setTtlSec(0) // user responsible for closing session
            .build()
    }

    /**
     * Generate a subscribe config
     * @param serviceName The service name to subscribe to
     * @param filter An optional list of other services to also subscribe to
     * @return A SubscribeConfig
     */
    fun buildSubscribeConfig(serviceName: String, filter: List<String>): SubscribeConfig {
        return SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .setServiceSpecificInfo(getDeviceId().toByteArray())
            .setMatchFilter(filter.map {it.toByteArray()})
            .setSubscribeType(SubscribeConfig.SUBSCRIBE_TYPE_PASSIVE)
            .setTerminateNotificationEnabled(true)
            .setTtlSec(0) // user responsible for closing session
            .build()
    }

    /**
     * Checks if WiFi Aware is available on this device
     * @return true if available else false
     */
    fun isAvailable(context: Context): Boolean {
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

