package rr.rms.wifiaware.library

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeDiscoverySession
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rr.rms.R
import rr.rms.wifiaware.WifiAwareActivity
import rr.rms.wifiaware.library.test.MessageUtils
import timber.log.Timber

class WifiAwareForegroundService: Service() {

    private val CHANNEL_ID = "RMS"
    private var currentState = STATE.START
    private var msgQueue = Array(16) { MessageUtils.randomMessage() }

//    var receivedMessages = mutableListOf<Message>()

    enum class STATE {
        START,
        PUBLISHING,
        SUBSCRIBING,
        PUBLISHING_AND_SUBSCRIBING,
        SYNCING_STARTED_PUBLISHER,
        SYNCING_STARTED_SUBSCRIBER,
        SYNCING_DONE_PUBLISHER,
        SYNCING_DONE_SUBSCRIBER,
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
            WifiAwareClient.closeAwareSession()
        }

        // test function
        fun startClient () {
            CoroutineScope(Dispatchers.Main).launch {
                WifiAwareClient.receiveData(object : WifiAwareClient.DataCallback{
                    override fun onSuccess(data: ByteArray) {
                        Logger.log(Logger.ACTIONS.SYNC_RCV, Logger.me(), "dont know", String(data))
                    }
                })
            }
        }

        // test function
        fun startServer() {
            CoroutineScope(Dispatchers.Main).launch {
                WifiAwareClient.sendData("hello sir".toByteArray())
            }
        }

        // test function
        fun startPublishing() {
            WifiAwareClient.publish("rms", "i am the publisher", null)
        }

        // test function
        fun startSubscribing() {
            WifiAwareClient.subscribe("rms", emptyList(), Logger.me(), null)
        }

        // test function
        fun closeSession() {
            WifiAwareClient.closeAwareSession()
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
            STATE.START -> {
//                clear()
//                nextState(STATE.PUBLISHING_AND_SUBSCRIBING)
            }
            STATE.PUBLISHING -> {
                startPublishing()
            }
            STATE.SUBSCRIBING -> {
                startSubscribing()
            }
            STATE.PUBLISHING_AND_SUBSCRIBING -> {
                startPublishing()
                startSubscribing()
            }
            STATE.SYNCING_STARTED_PUBLISHER -> {
//                startSyncingServer()
            }
            STATE.SYNCING_STARTED_SUBSCRIBER -> {
//                startSyncingClient()
            }
            STATE.SYNCING_DONE_PUBLISHER -> {
//                clear()
//                nextState(STATE.PUBLISHING)
            }
            STATE.SYNCING_DONE_SUBSCRIBER -> {
//                clear()
//                nextState(STATE.SUBSCRIBING)
            }
            STATE.DISCONNECTED -> {
                // todo error handling
                nextState(STATE.START)
            }
        }
        Timber.d("state machine state: $currentState")
    }

//    private fun clear() {
////        WifiAwareClient.closeAwareSession() // todo should be handled differently
//    }

    private fun startPublishing() {
        WifiAwareClient.publish("rms", "i am the publisher", object : WifiAwareClient.PublishCallback{
            override fun onMessageReceived(
                publishDiscoverySession: PublishDiscoverySession?,
                peerHandle: PeerHandle?,
                msgRcvd: ByteArray
            ) {
                nextState(STATE.SYNCING_STARTED_PUBLISHER)
            }

            override fun onMessageSent(msgSent: String) {
            }

            override fun onError(msg: String) {
                Timber.e("publish error: $msg")
            }
        })
    }

    private fun startSubscribing() {
        WifiAwareClient.subscribe("rms", emptyList(), Logger.me(), object : WifiAwareClient.SubscribeCallback{
            override fun onMessageReceived(
                subscribeDiscoverySession: SubscribeDiscoverySession?,
                peerHandle: PeerHandle?,
                msgRcvd: ByteArray
            ) {
                nextState(STATE.SYNCING_STARTED_SUBSCRIBER)
            }

            override fun onMessageSent(msgSent: String) {}

            override fun onError(msg: String) {
                Timber.e("subscribe error: $msg")
            }
        })
    }

//    private fun startSyncingServer() {
//        WifiAwareClient.getSocket(applicationContext, true, object : WifiAwareClient.SocketCallback{
//            override fun onResponse(socket: Socket?) {
//                Timber.d("publisher network socket onResponse()")
//
//                // publisher sends data
//                NetworkUtils.sendBytes(socket, "hello".toByteArray())
//                Logger.log(Logger.ACTIONS.SYNC_SEND, Logger.me(), "dont know", msgQueue.toString())
//            }
//        })
//    }
//
//    private fun startSyncingClient() {
//        WifiAwareClient.getSocket(applicationContext, false, object : WifiAwareClient.SocketCallback{
//            override fun onResponse(socket: Socket?) {
//                Timber.d("subscriber network socket onResponse()")
//
//                // subscriber receives data
//                val data = NetworkUtils.receiveBytes(socket)
//                Logger.log(Logger.ACTIONS.SYNC_RCV, "dont know", Logger.me(), "data.toMessages().toString()")
//            }
//        })
//    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}