package rr.rms.wifiaware.library

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.aware.WifiAwareSession
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import rr.rms.R
import rr.rms.wifiaware.library.aware.SessionManager
import rr.rms.wifiaware.library.test.WifiAwareActivity
import timber.log.Timber

class WifiAwareForegroundService: Service() {

    private val CHANNEL_ID = "RMS"
    private var currentState = STATE.START
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    enum class STATE {
        START,
        AWARE_SESSION,
        PUBLISHING,
        SUBSCRIBING,
        PUBLISHING_AND_SUBSCRIBING,
        DISCONNECTED,
    }

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, WifiAwareForegroundService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, WifiAwareForegroundService::class.java)
            context.stopService(stopIntent)
            SessionManager.close()
        }

        fun sessionTest() {
            SessionManager.getLatestSession(object : SessionManager.Session{
                override fun onSession(session: WifiAwareSession) {
                    Timber.d("got session")
                }
            })
        }

        fun publishTest() {
            SessionManager.getSessionAndPublish("rmsmsg","i am the publisher")
        }

        fun subscribeTest() {
            SessionManager.getSessionAndSubscribe("rmsmsg", emptyList(),"i am the subscriber")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, WifiAwareActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification =  NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wifi Aware Network")
            .setContentText("Wifi Aware Network Running...")
            .setSmallIcon(R.drawable.wifi_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
//        nextState(STATE.START) todo temporarily do nothing
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     *  Given the current state, this gets the next state
     */
    private fun nextState(state: STATE) {
        Timber.d("state machine state: $state")
        currentState = state
        when (currentState) {
            STATE.START -> nextState(STATE.AWARE_SESSION)
            STATE.AWARE_SESSION -> startWifiAwareSession()
            STATE.PUBLISHING_AND_SUBSCRIBING -> {
                startPublishing()
                startSubscribing()
            }
            STATE.PUBLISHING -> startPublishing()
            STATE.SUBSCRIBING -> startSubscribing()
            STATE.DISCONNECTED -> nextState(STATE.START)
        }
    }

    private fun startWifiAwareSession() {
        SessionManager.getLatestSession(object : SessionManager.Session{
            override fun onSession(session: WifiAwareSession) {
                nextState(STATE.PUBLISHING_AND_SUBSCRIBING)
            }
        })
    }

    private fun startPublishing() {
        scope.launch {
            SessionManager.getSessionAndPublish("rmsmsg","i am the publisher")
        }
    }

    private fun startSubscribing() {
        scope.launch {
            SessionManager.getSessionAndSubscribe("rmsmsg", emptyList(),"i am the publisher")
        }
    }


    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}