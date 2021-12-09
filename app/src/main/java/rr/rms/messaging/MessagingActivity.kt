package rr.rms.messaging

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import rr.rms.MainApplication
import rr.rms.R
import rr.rms.wifiaware.library.WifiAwareForegroundService
import rr.rms.wifiaware.library.aware.WifiAwareUtils
import rr.rms.wifiaware.library.models.Message

class MessagingActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 101
    lateinit var messagingViewModel: MessagingViewModel
    lateinit var receivedMsgsAdapter: MessagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        setSupportActionBar(findViewById(R.id.toolbar))

        val msgEditText = findViewById<EditText>(R.id.messageText)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val restartButton = findViewById<Button>(R.id.restartButton)

        sendButton.setOnClickListener {
            val msgText = msgEditText.text.toString()
            messagingViewModel.sendMessage(Message(msgText))
        }

        refreshButton.setOnClickListener {
            messagingViewModel.getReceivedMessages()
        }

        restartButton.setOnClickListener {
            WifiAwareForegroundService.stopService(MainApplication.applicationContext)
            WifiAwareForegroundService.startService(MainApplication.applicationContext)
        }

        WifiAwareUtils.setupPermissions(PERMISSION_REQUEST_CODE,this)

        // recylcler view setup
        receivedMsgsAdapter = MessagingAdapter()
        val msgRecyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        with(msgRecyclerView){
            adapter = receivedMsgsAdapter
            layoutManager = LinearLayoutManager(this@MessagingActivity)
        }

        // viewmodel setup
        messagingViewModel = ViewModelProvider(this)[MessagingViewModel::class.java]
        messagingViewModel.receivedMessages.observe(this,  { msgs ->
            receivedMsgsAdapter.addMessages(msgs)
        })

        WifiAwareForegroundService.startService(MainApplication.applicationContext)
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
}