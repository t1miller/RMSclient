package rr.rms.messaging

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import rr.rms.messaging.models.Message
import timber.log.Timber

class MessagingViewModel(application: Application): AndroidViewModel(application) {

    val receivedMessages: MutableLiveData<List<Message>> by lazy {
        MutableLiveData(mutableListOf())
    }

    fun updateReceivedMessages(msgs: List<Message>) {
        Timber.d("getReceivedMessages() msgs: $msgs")
        receivedMessages.postValue(msgs)
    }
}