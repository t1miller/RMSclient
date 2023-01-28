package rr.rms.wifiaware.library.test


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rr.rms.R
import rr.rms.wifiaware.library.*
import rr.rms.wifiaware.library.aware.WifiAwareUtils
import rr.rms.wifiaware.library.logging.Logger
import timber.log.Timber


class WifiAwareActivity : AppCompatActivity(), Logger.LoggerCallback {

    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var logTextView: TextView

//    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if(WifiAwareUtils.isAvailable(applicationContext)){
//                WifiAwareForegroundService.startService(applicationContext)
//            } else {
//                Timber.e("wifi aware still not available")
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_aware)

        logTextView = findViewById(R.id.log_textview)

        val saveLogsButton = findViewById<Button>(R.id.saveLogsButton)
        saveLogsButton.setOnClickListener {
            Logger.writeLogsToDisk()
        }

        val shareLogsButton = findViewById<Button>(R.id.shareLogsButton)
        shareLogsButton.setOnClickListener {
            Logger.shareLogs(this)
        }

        val clearLogsButton = findViewById<Button>(R.id.clearLogsButton)
        clearLogsButton.setOnClickListener {
            Logger.clearLogs()
        }

        val stopServiceButton = findViewById<Button>(R.id.stopServiceButton)
        stopServiceButton.setOnClickListener {
            WifiAwareForegroundService.stopService(this)
        }

        val startSessionButton = findViewById<Button>(R.id.sessionButton)
        startSessionButton.setOnClickListener {
            WifiAwareForegroundService.sessionTest()
        }

        val startSubscriberButton = findViewById<Button>(R.id.subscribeButton)
        startSubscriberButton.setOnClickListener {
            WifiAwareForegroundService.subscribeTest()
        }

        val startPublisherButton = findViewById<Button>(R.id.publishButton)
        startPublisherButton.setOnClickListener {
            WifiAwareForegroundService.publishTest()
        }

        // permission check
        WifiAwareUtils.setupPermissions(PERMISSION_REQUEST_CODE, this)

//        registerReceiver(receiver, IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED))

        // check wifi hardware
        if(WifiAwareUtils.isAvailable(this)) {
            Toast.makeText(this, "error, no wifi aware service", Toast.LENGTH_LONG).show()
            Timber.e("error no wifi aware service")
        }

        WifiAwareForegroundService.startService(this)

        Logger.addListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // dont care
            }
        }
    }

    override fun onLogAdded(log: Logger.Log) {
        runOnUiThread {
            logTextView.append("$log\n")
        }
    }
}
