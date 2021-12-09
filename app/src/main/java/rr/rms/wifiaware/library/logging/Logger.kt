package rr.rms.wifiaware.library.logging

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import rr.rms.BuildConfig
import rr.rms.MainApplication
import rr.rms.wifiaware.library.aware.WifiAwareUtils
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.*

object Logger {

    private const val filename = "/Logs.txt"
    private val fullFileName = MainApplication.applicationContext.filesDir.absolutePath + filename
    private var logs = mutableListOf<Log>()
    private var callback: LoggerCallback? = null

    interface LoggerCallback {
        fun onLogAdded(log: Log)
    }

    enum class ACTIONS{
        PUBLISH,
        PUBLISH_MSG,
        PUBLISH_MSG_MSG,
        SUBSCRIBE,
        SUBSCRIBE_MSG,
        SUBSCRIBE_MSG_MSG,
        SYNC_SEND,
        SYNC_RCV,
    }

    @Serializable
    data class Log(
        val action: ACTIONS,
        val src: String,
        val dst: String,
        val time: String,
        val payload: String
    )

    fun addListener(callback: LoggerCallback) {
        Logger.callback = callback
    }

    fun log(action: ACTIONS, src: String, dst: String, payload: String) {
        val log = Log(action, src, dst, Calendar.getInstance().timeInMillis.toString(), payload)
        logs.add(log)
        callback?.onLogAdded(log)
        Timber.d("added to log: $log")
    }

    fun me() : String {
        return WifiAwareUtils.getDeviceId()
    }

    fun writeLogsToDisk() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                logs.toFile()
            }
        }
    }

    fun clearLogs() {
        logs.clear()
        writeLogsToDisk()
    }

    fun shareLogs(activity: Activity) {
        val statURI = FileProvider.getUriForFile(
            MainApplication.applicationContext,
            BuildConfig.APPLICATION_ID + ".provider",
            File(fullFileName)
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, statURI)
        }
        activity.startActivity(Intent.createChooser(shareIntent, "share logs with:"))
    }

    private fun MutableList<Log>.toFile() {
        try {
            File(fullFileName).printWriter().use { out ->
                out.write(this.toJSON())
            }
        } catch (e: Exception) {
            Timber.e("error writing log file")
        }
    }

    private fun MutableList<Log>.toJSON(): String{
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)  // json string
    }
}