package rr.rms.wifiaware.library.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Message(
    var src: String,
    var dst: String,
    var id: String,
    var msg: String
)

fun List<Message>.toByteArray(): ByteArray {
    val jsonList = Json.encodeToString(this)
    return jsonList.toByteArray()
}

fun ByteArray.toMessages() : MutableList<Message>{
    return Json.decodeFromString(String(this))
}