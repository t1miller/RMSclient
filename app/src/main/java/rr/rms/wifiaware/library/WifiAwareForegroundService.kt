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
import rr.rms.R
import rr.rms.wifiaware.WifiAwareActivity
import rr.rms.wifiaware.library.models.toMessages
import rr.rms.wifiaware.library.test.MessageUtils
import timber.log.Timber
import java.net.Socket

class WifiAwareForegroundService: Service() {

    private val CHANNEL_ID = "RMS"
    private var currentState = STATE.START
    private var mPublishDiscoverySession: PublishDiscoverySession? = null
    private var mSubscribeDiscoverySession: SubscribeDiscoverySession? = null
    private var mPublishPeerHandle: PeerHandle? = null
    private var mSubscribePeerHandle: PeerHandle? = null
    var msgQueue = Array(16) { MessageUtils.randomMessage() }
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
            // todo close all wifi aware sessions
            val stopIntent = Intent(context, WifiAwareForegroundService::class.java)
            context.stopService(stopIntent)
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
                clear()
                nextState(STATE.PUBLISHING_AND_SUBSCRIBING)
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
                startSyncingServer()
            }
            STATE.SYNCING_STARTED_SUBSCRIBER -> {
                startSyncingClient()
            }
            STATE.SYNCING_DONE_PUBLISHER -> {
                clear()
                nextState(STATE.PUBLISHING)
            }
            STATE.SYNCING_DONE_SUBSCRIBER -> {
                clear()
                nextState(STATE.SUBSCRIBING)
            }
            STATE.DISCONNECTED -> {
                // todo error handling
                nextState(STATE.START)
            }
        }
        Timber.d("state machine state, start: $state end: $currentState")
    }

    private fun clear() {
        mSubscribePeerHandle = null
        mPublishPeerHandle = null
        mPublishDiscoverySession = null
        mSubscribeDiscoverySession = null
    }

    private fun startPublishing() {
        var msgDst = ""
        WifiAwareClient.publish("rms", "i am the publisher", object : WifiAwareClient.PublishCallback{
            override fun onMessageReceived(
                publishDiscoverySession: PublishDiscoverySession?,
                peerHandle: PeerHandle?,
                msgRcvd: ByteArray
            ) {
                mPublishPeerHandle = peerHandle
                mPublishDiscoverySession = publishDiscoverySession
                msgDst = String(msgRcvd)
                Logger.log(Logger.ACTIONS.PUBLISH_MSG, Logger.me(), msgDst, msgDst)
                nextState(STATE.SYNCING_STARTED_PUBLISHER)
            }

            override fun onMessageSent(msgSent: String) {
                Logger.log(Logger.ACTIONS.PUBLISH_MSG_MSG, Logger.me(), msgDst, msgSent)
            }

            override fun onError(msg: String) {
                Timber.e("publish error: $msg")
            }
        })
        Logger.log(Logger.ACTIONS.PUBLISH, Logger.me(), "everyone", "publishing")
    }

    private fun startSubscribing() {
        var msgDst = ""
        WifiAwareClient.subscribe("rms", emptyList(), Logger.me(), object : WifiAwareClient.SubscribeCallback{
            override fun onMessageReceived(
                subscribeDiscoverySession: SubscribeDiscoverySession?,
                peerHandle: PeerHandle?,
                msgRcvd: ByteArray
            ) {
                mSubscribePeerHandle = peerHandle
                mSubscribeDiscoverySession = subscribeDiscoverySession
                msgDst = String(msgRcvd)
                Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG_MSG, Logger.me(), msgDst, msgDst)
                nextState(STATE.SYNCING_STARTED_SUBSCRIBER)
            }

            override fun onMessageSent(msgSent: String) {
                Logger.log(Logger.ACTIONS.SUBSCRIBE_MSG, msgSent, msgDst, msgSent)
            }

            override fun onError(msg: String) {
                Timber.e("subscribe error: $msg")
            }
        })
        Logger.log(Logger.ACTIONS.SUBSCRIBE, Logger.me(),"everyone", "subscribing")
    }

    private fun startSyncingServer() {
        WifiAwareClient.getNetworkSocket(applicationContext, true, mPublishDiscoverySession, mPublishPeerHandle, object : WifiAwareClient.SocketCallback{
            override fun onResponse(socket: Socket?) {
                Timber.d("publisher network socket onResponse()")

                // publisher sends data
                NetworkUtils.sendBytes(socket, "hello".toByteArray())
                Logger.log(Logger.ACTIONS.SYNC_SEND, Logger.me(), "dont know", msgQueue.toString())
            }
        })
    }

    private fun startSyncingClient() {
        WifiAwareClient.getNetworkSocket(applicationContext, false, mSubscribeDiscoverySession, mSubscribePeerHandle, object : WifiAwareClient.SocketCallback{
            override fun onResponse(socket: Socket?) {
                Timber.d("network socket onResponse()")

                // subscriber receives data
                val data = NetworkUtils.receiveBytes(socket)
                Logger.log(Logger.ACTIONS.SYNC_RCV, "dont know", Logger.me(), data.toMessages().toString())
            }
        })
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}