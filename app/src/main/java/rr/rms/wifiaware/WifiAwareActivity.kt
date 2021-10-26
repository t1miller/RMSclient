package rr.rms.wifiaware


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import rr.rms.R
import rr.rms.wifiaware.library.*
import timber.log.Timber


class WifiAwareActivity : AppCompatActivity(), Logger.LoggerCallback {

    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var logTextView: TextView

//    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
////            appendToUiLog("Wifi Aware is ${WifiAwareUtils.isAvailable(context)}")
//            // todo shouldnt publish subscribe logic go in here, when the hardware is available?
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


        // permission check
        setupPermissions()

        WifiAwareForegroundService.startService(this)

        Logger.addListener(this)
    }

    override fun onPause() {
        super.onPause()
//        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
//        registerReceiver(receiver, IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED))
    }

    private fun setupPermissions() {
        val permissionFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionFine != PackageManager.PERMISSION_GRANTED){
            Timber.d("need to ask user for permission")
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
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
        logTextView.append("$log\n")
    }
}
