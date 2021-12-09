package rr.rms.wifiaware.library.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rr.rms.wifiaware.library.logging.Logger
import rr.rms.wifiaware.library.models.toMessages
import java.net.Socket

class Client(private val socket: Socket) {

    interface ReceiveDataCallback {
        fun onSuccess(dataReceived: ByteArray)
    }

    suspend fun receiveData(callback: ReceiveDataCallback) {
        withContext(Dispatchers.IO) {
            val data = NetworkUtils.receiveBytes(socket)
            callback.onSuccess(data)
            Logger.log(Logger.ACTIONS.SYNC_RCV, address(), Logger.me(), data.toMessages().toString())
        }
    }

    fun address(): String {
        return socket.remoteSocketAddress.toString()
    }

    fun close() {
        socket.close()
    }
}