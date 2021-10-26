package rr.rms.wifiaware.library

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import rr.rms.MainApplication
import timber.log.Timber
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.measureTimeMillis

object NetworkUtils {

    fun getAPort() : Int {
        val ss = ServerSocket(0)
        return ss.localPort
    }

    fun receiveBytes(socket: Socket?) : ByteArray{
        if(socket == null){
            Timber.e("socket null cant receiveBytes()")
        }
        var bytesRead : ByteArray?
        val timeInMs = measureTimeMillis {
            val inputStream = socket?.getInputStream()
            bytesRead = inputStream?.readBytes()
        }
        Timber.d("receiveBytes() bytes = ${bytesRead?.size} in ms = $timeInMs")
        return bytesRead ?: ByteArray(0)
    }

    fun sendBytes(socket: Socket?, byteArray: ByteArray) {
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
}