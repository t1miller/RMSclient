package rr.rms.wifiaware.library.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rr.rms.wifiaware.library.logging.Logger
import rr.rms.wifiaware.library.models.toMessages
import java.net.Socket

class Server(private val socket: Socket) {

    interface SendDataCallback {
        fun onSuccess()
    }

    suspend fun sendData(data: ByteArray, callback: SendDataCallback) {
        withContext(Dispatchers.IO) {
            NetworkUtils.sendBytes(socket, data)
            callback.onSuccess()
            Logger.log(Logger.ACTIONS.SYNC_SEND, Logger.me(), address(), data.toMessages().toString())
        }
    }

    fun address(): String {
        return socket.remoteSocketAddress.toString()
    }

    fun close() {
        socket.close()
    }
}