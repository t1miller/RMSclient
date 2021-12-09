package rr.rms.wifiaware.library.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class Message(
    var msg: String,
    var user: String = "Trent:",
    var id: String = UUID.randomUUID().toString(),
    var time: Long = System.currentTimeMillis()
)

fun MutableList<Message>.toByteArray(): ByteArray {
    val jsonList = Json.encodeToString(this)
    return jsonList.toByteArray()
}

fun ByteArray.toMessages() : MutableList<Message>{
    return Json.decodeFromString(String(this))
}