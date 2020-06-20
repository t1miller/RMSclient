package rr.rms.utils

import timber.log.Timber
import java.net.Socket

object NetworkUtils {

    fun receiveBytes(socket: Socket) : ByteArray?{
        val inputStream = socket.getInputStream()
        return inputStream.readBytes()
    }

    fun sendBytes(socket: Socket, byteArray: ByteArray) {
        val outputStream = socket.getOutputStream()
        try {
            outputStream.write(byteArray)
        }catch (e: Exception) {
            Timber.e(e)
        }finally {
            outputStream.close()
        }
    }
}