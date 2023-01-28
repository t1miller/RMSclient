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
import rr.rms.messaging.models.Message
import timber.log.Timber

class MessagingActivity : AppCompatActivity(), MessagingCache.MessagingCacheListener {

    private val PERMISSION_REQUEST_CODE = 101
    lateinit var messagingViewModel: MessagingViewModel
    lateinit var messageAdapter: MessagingAdapter

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
            MessagingCache.addMessagesSend(listOf(Message(msgText)))
        }

        refreshButton.setOnClickListener {
           // todo
        }

        restartButton.setOnClickListener {
            WifiAwareForegroundService.stopService(MainApplication.applicationContext)
            WifiAwareForegroundService.startService(MainApplication.applicationContext)
        }

        WifiAwareUtils.setupPermissions(PERMISSION_REQUEST_CODE,this)

        // recylcler view setup
        messageAdapter = MessagingAdapter()
        val msgRecyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        with(msgRecyclerView){
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MessagingActivity)
        }

        // viewmodel setup
        messagingViewModel = ViewModelProvider(this)[MessagingViewModel::class.java]
        messagingViewModel.receivedMessages.observe(this,  { msgs ->
            messageAdapter.addMessages(msgs)
        })

        MessagingCache.addCacheChangedListener(this)
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

    override fun onMessagesReceived(msgs: List<Message>) {
        Timber.d("updating viewmodel w/ messages: $msgs")
        messagingViewModel.updateReceivedMessages(msgs)
    }
}