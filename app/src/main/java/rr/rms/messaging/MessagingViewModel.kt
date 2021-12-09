package rr.rms.messaging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import rr.rms.wifiaware.library.models.Message
import rr.rms.wifiaware.library.net.ServerClientManager
import rr.rms.wifiaware.library.net.ServerClientManager.flatten
import timber.log.Timber

class MessagingViewModel(application: Application): AndroidViewModel(application) {

    val receivedMessages: MutableLiveData<MutableList<Message>> by lazy {
        MutableLiveData(mutableListOf())
    }

    fun sendMessage(msg: Message) {
        // add msg to outgoing queue
        ServerClientManager.addSendMessage(msg)
    }

    fun getReceivedMessages() {
        val newMsgsHash = ServerClientManager.getReceivedMessages()
        val newMsgsList = newMsgsHash.flatten()
        val originalMsgs = receivedMessages.value
        originalMsgs?.addAll(newMsgsList)

        Timber.d("getReceivedMessages() msgs: $originalMsgs")
        receivedMessages.value = originalMsgs
    }

//    override fun onReceived(newMsgs: MutableList<Message>) {
//        val originalMsgs = receivedMessages.value
//        originalMsgs?.addAll(newMsgs)
//        receivedMessages.value = originalMsgs
//    }
}