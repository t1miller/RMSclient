package rr.rms.chord

import android.content.Context

/**
 * Request data structure
 */
data class ChordRequestData(val src: Node,
                            val dst: Node,
                            val msg: String,
                            val type: ChordRequestTypes)

/**
 * Preferred transport method
 */
enum class ChordRequestTypes {
    WIFIAWARE,
    INTERNET
}

/**
 * Common interface that all clients implement
 */
interface ChordRequest {
    fun send(context: Context?, data: ChordRequestData)
}
