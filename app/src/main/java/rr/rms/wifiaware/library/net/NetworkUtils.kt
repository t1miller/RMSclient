package rr.rms.wifiaware.library.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import rr.rms.MainApplication
import rr.rms.wifiaware.library.logging.Logger
import rr.rms.messaging.models.toMessages
import timber.log.Timber
import java.net.Socket
import kotlin.system.measureTimeMillis

object NetworkUtils {

    // coroutine that waits for receiveBytes() to finish
    suspend fun receiveData(socket: Socket?) = coroutineScope {
        val deferred = async { receiveBytes(socket) }
        deferred.await()
    }

    // coroutine that receive bytes on IO background thread
    private suspend fun receiveBytes(socket: Socket?): ByteArray = withContext(Dispatchers.IO){
        if(socket == null){
            Timber.e("socket null cant receiveBytes()")
        }
        var bytesRead : ByteArray?
        val timeInMs = measureTimeMillis {
            val inputStream = socket?.getInputStream()
            bytesRead = inputStream?.readBytes()
        }
        Timber.d("receiveBytes() bytes = ${bytesRead?.size} in ms = $timeInMs")
        Logger.log(Logger.ACTIONS.SYNC_RCV, address(socket), Logger.me(), bytesRead?.toMessages().toString())
        bytesRead ?: ByteArray(0)
    }

    // coroutine that waits for sendBytes() to finish
    suspend fun sendData(socket: Socket?, data: ByteArray) = coroutineScope {
        val deferred = async { sendBytes(socket, data) }
        deferred.await()
        Logger.log(Logger.ACTIONS.SYNC_SEND, Logger.me(), address(socket), data.toMessages().toString())
    }

    // coroutine that sends bytes on IO background thread
    private suspend fun sendBytes(socket: Socket?, byteArray: ByteArray) = withContext(Dispatchers.IO) {
        if(socket == null){
            Timber.e("socket null cant sendBytes()")
        }
        val timeInMs = measureTimeMillis {
            val outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(byteArray)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                outputStream?.close()
            }
        }
        Timber.d("sendBytes() bytes = ${byteArray.size} in ms = $timeInMs")
    }

    fun hasInternet() : Boolean {
        val connectivityManager = MainApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun address(socket: Socket?): String {
        return socket?.remoteSocketAddress.toString()
    }
}