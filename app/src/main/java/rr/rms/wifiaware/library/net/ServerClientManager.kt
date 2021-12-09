package rr.rms.wifiaware.library.net

import rr.rms.wifiaware.library.models.Message
import rr.rms.wifiaware.library.models.toByteArray
import rr.rms.wifiaware.library.models.toMessages
import timber.log.Timber

// todo broadcast results instead of callback
object ServerClientManager {
    private val clientQueue = ArrayDeque<Client>()
    private val serverQueue = ArrayDeque<Server>()

    // list of messages you generated to send to everyone
    private val sendMsg = mutableListOf<Message>()

    // list of messages you generate that were actually sent and to whom
    private val sentMsgsTo = hashMapOf<String, MutableList<Message>>()

    // list of messages others have sent to everyone
    private val receivedMessages = hashMapOf<String, MutableList<Message>>()

    interface ServerClientCallback {
        fun done()
    }

    fun addSendMessage(msg: Message) {
        sendMsg.add(msg)
    }

    fun getReceivedMessages(): HashMap<String, MutableList<Message>> {
        return receivedMessages
    }

    fun addClient(client: Client) {
        clientQueue.add(client)
    }

    fun addServer(server: Server) {
        serverQueue.add(server)
    }

    // send all data to clients
    suspend fun sendDataToClients(callback: ServerClientCallback) {

        if(sendMsg.isEmpty()) {
            Timber.d("no messages to send")
        }

        while(serverQueue.size > 0){
            val server = serverQueue.removeFirst()
            val address = server.address()
            server.sendData(sendMsg.toByteArray(), object: Server.SendDataCallback {
                override fun onSuccess() {
                    Timber.d("sent data to $address")
                    sentMsgsTo[address]?.addAll(sendMsg)
                }
            })
            server.close()
        }
        callback.done()
    }

    // receive all data from servers
    suspend fun receiveDataFromServers(callback: ServerClientCallback) {
        while(clientQueue.size > 0){
            val client = clientQueue.removeFirst()
            client.receiveData(object: Client.ReceiveDataCallback{
                override fun onSuccess(dataReceived: ByteArray) {
                    val address = client.address()
                    val msgs = dataReceived.toMessages()
                    Timber.d("received data from $address")
                    receivedMessages[address]?.addAll(msgs)
                }
            })
            client.close()
        }
        callback.done()
    }

    fun HashMap<String, MutableList<Message>>.flatten(): MutableList<Message> {
        val values = mutableListOf<Message>()
        forEach { (_, v) ->
            values.addAll(v)
        }
        return values
    }
}
