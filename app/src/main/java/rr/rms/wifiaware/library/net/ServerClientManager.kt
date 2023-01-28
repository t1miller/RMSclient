package rr.rms.wifiaware.library.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import kotlinx.coroutines.*
import rr.rms.MainApplication
import rr.rms.messaging.MessagingCache
import rr.rms.messaging.models.toByteArray
import rr.rms.messaging.models.toMessages
import rr.rms.wifiaware.library.aware.WifiAwareUtils
import timber.log.Timber
import java.lang.Exception
import java.net.ServerSocket

object ServerClientManager {

    private val connectManager = MainApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

//    suspend fun sendDataToClientBlocking(publishSession: PublishDiscoverySession?, subscribePeerHandle: PeerHandle) = coroutineScope {
//        Timber.d("sending data to client")
//        val deferred = async { sendDataToClient(publishSession, subscribePeerHandle) }
//        deferred.await()
//    }

    fun sendDataToClient(publishSession: PublishDiscoverySession?, subscribePeerHandle: PeerHandle) {
        GlobalScope.launch {
            try {
                getServerSocket(publishSession, subscribePeerHandle)
            } catch (e: Exception){
                Timber.e("error server socket: $e")
            }
        }
    }

    private suspend fun getServerSocket(publishSession: PublishDiscoverySession?, subscribePeerHandle: PeerHandle) = withContext(Dispatchers.IO) {
        Timber.d("getting server socket")
        val serverSocket = ServerSocket(0)
        val networkRequest = WifiAwareUtils.buildPublisherNetworkRequest(publishSession, subscribePeerHandle, serverSocket.localPort)
        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onUnavailable() {
                Timber.e("server socket unavailable")
//                connectManager.unregisterNetworkCallback(this)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val serverSocketAccepted = serverSocket.accept()
                Timber.d("server socket: port used = ${serverSocketAccepted.port} peerIpv6 = ${peerAwareInfo.peerIpv6Addr} peerPort = ${peerAwareInfo.port}")
                GlobalScope.launch {
                    val data = MessagingCache.getMessagesAll()
                    Timber.d("server socket not null, sent data to client, data = $data")
                    NetworkUtils.sendData(serverSocketAccepted, data.toByteArray())
//                    serverSocket?.close()
                }
            }

            override fun onLost(network: Network) {
                Timber.e("server socket onLost()")
//                connectManager.unregisterNetworkCallback(this)
            }
        }

        if (networkRequest != null) {
            Timber.d("server network request sent")
            connectManager.requestNetwork(networkRequest, callback)
        }
    }

//    suspend fun receiveDataFromServerBlocking(subscribeSession: SubscribeDiscoverySession?, publishPeerHandle: PeerHandle?) = coroutineScope {
//        Timber.d("receiving data from server")
//        val deferred = async { receiveDataFromServer(subscribeSession, publishPeerHandle, this) }
//        deferred.await()
//    }

    suspend fun receiveDataFromServer(subscribeSession: SubscribeDiscoverySession?, publishPeerHandle: PeerHandle?, scope: CoroutineScope) = withContext(Dispatchers.IO) {
        try {
            getClientSocket(subscribeSession, publishPeerHandle, scope)
        } catch (e: Exception){
            Timber.e("error server socket: $e")
        }
    }

    private suspend fun getClientSocket(subscribeSession: SubscribeDiscoverySession?, publishPeerHandle: PeerHandle?, scope: CoroutineScope) = withContext(Dispatchers.IO) {
        Timber.d("getting client socket")
        val networkRequest: NetworkRequest? = WifiAwareUtils.buildSubscriberNetworkRequest(subscribeSession, publishPeerHandle)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                Timber.e("client socket unavailable")
//                connectManager.unregisterNetworkCallback(this)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port
                val clientSocket = network.socketFactory.createSocket(peerIpv6, peerPort)
                Timber.d("client socket: port used = ${clientSocket.port} peerIpv6 = $peerIpv6 peerPort = ${peerAwareInfo.port}")
                scope.launch {
                    val data = NetworkUtils.receiveData(clientSocket)
                    Timber.d("client socket not null, received data from server = $data")
                    MessagingCache.addMessagesReceived(data.toMessages())
//                    clientSocket?.close()
                }
            }

            override fun onLost(network: Network) {
                Timber.e("client socket onLost()")
//                connectManager.unregisterNetworkCallback(this)
            }
        }

        if (networkRequest != null) {
            Timber.d("client network request sent")
            connectManager.requestNetwork(networkRequest, callback)
        }
    }
}