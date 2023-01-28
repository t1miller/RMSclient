package rr.rms.messaging

import rr.rms.messaging.models.Message
import rr.rms.messaging.models.MessageUtils
import timber.log.Timber

object MessagingCache {

    interface MessagingCacheListener {
        fun onMessagesReceived(msgs: List<Message>)
    }

    private val messagesToSend = mutableSetOf(
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage(),
        MessageUtils.randomMessage()
    )
    private val messagesReceived = mutableSetOf<Message>()
    private var cacheChangedListeners = mutableListOf<MessagingCacheListener>()

    fun addMessagesSend(msgs: List<Message>) {
        Timber.d("messages added to cache: $msgs")
        messagesToSend.addAll(msgs)
        notifyListeners() // todo should remove this after testing is done
    }

    fun addMessagesReceived(msgs: List<Message>) {
        Timber.d("messages received: $msgs")
        messagesReceived.addAll(msgs)
        notifyListeners()
    }

    fun getMessagesAll(): List<Message> {
        val allMsgs = messagesReceived.toMutableSet()
        allMsgs.addAll(messagesToSend)
        Timber.d("messages all: $allMsgs")
        return allMsgs.toList()
    }

    fun addCacheChangedListener(listener: MessagingCacheListener) {
        cacheChangedListeners.add(listener)
    }

    private fun notifyListeners() {
        cacheChangedListeners.onEach {
            it.onMessagesReceived(messagesReceived.toList())
        }
    }
}