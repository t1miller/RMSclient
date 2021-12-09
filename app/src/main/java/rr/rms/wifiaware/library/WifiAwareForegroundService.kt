package rr.rms.wifiaware.library

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rr.rms.R
import rr.rms.wifiaware.library.test.WifiAwareActivity
import rr.rms.wifiaware.library.aware.WifiAwareManager
import rr.rms.wifiaware.library.net.Client
import rr.rms.wifiaware.library.net.ServerClientManager
import rr.rms.wifiaware.library.net.Server
import timber.log.Timber
import java.net.Socket

class WifiAwareForegroundService: Service() {

    private val CHANNEL_ID = "RMS"
    private var currentState = STATE.START

    enum class STATE {
        START,
        PUBLISHING,
        SUBSCRIBING,
        PUBLISHING_AND_SUBSCRIBING,
        SERVER_STARTED,
        CLIENT_STARTED,
        SERVER_AND_CLIENT_STARTED,
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
            WifiAwareManager.closeAwareSession()
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
        nextState(STATE.START)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     *  Given the current state, this gets the next state
     */
    private fun nextState(state: STATE) {
        currentState = state
        when (currentState) {
            STATE.START -> nextState(STATE.PUBLISHING_AND_SUBSCRIBING)
            STATE.PUBLISHING -> startPublishing()
            STATE.SUBSCRIBING -> startSubscribing()
            STATE.PUBLISHING_AND_SUBSCRIBING -> {
                startPublishing()
                startSubscribing()
            }
            STATE.DISCONNECTED -> nextState(STATE.START)
            STATE.SERVER_STARTED -> startServers()
            STATE.CLIENT_STARTED -> startClients()
            STATE.SERVER_AND_CLIENT_STARTED -> {
                startClients()
                startServers()
            }
        }
        Timber.d("state machine state: $currentState")
    }

    private fun startPublishing() {
        WifiAwareManager.publish("rms-msg", "i am the publisher", object : WifiAwareManager.PublishCallback{
            override fun onMessageSent(msgSent: String) {}

            override fun onServerSocket(socket: Socket?) {
                socket?.let {
                    ServerClientManager.addServer(Server(it))
                }
                nextState(STATE.SERVER_STARTED)
            }

            override fun onError(msg: String) {
                Timber.e("publish error: $msg")
            }
        })
    }

    private fun startSubscribing() {
        WifiAwareManager.subscribe("rms-msg", emptyList(),"i am the subscriber", object : WifiAwareManager.SubscribeCallback{
            override fun onMessageSent(msgSent: String) {}

            override fun onClientSocket(socket: Socket?) {
                socket?.let {
                    ServerClientManager.addClient(Client(it))
                }
                nextState(STATE.CLIENT_STARTED)
            }

            override fun onError(msg: String) {
                Timber.e("subscribe error: $msg")
            }
        })
    }

    private fun startServers() {
        CoroutineScope(Dispatchers.Main).launch {
            Timber.d("starting servers")
            ServerClientManager.sendDataToClients(object : ServerClientManager.ServerClientCallback{
                override fun done() {
                    //
                }
            })
        }
    }

    private fun startClients() {
        CoroutineScope(Dispatchers.Main).launch {
            Timber.d("starting clients")
            ServerClientManager.receiveDataFromServers(object : ServerClientManager.ServerClientCallback{
                override fun done() {
                    //
                }
            })
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}