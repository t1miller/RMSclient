package rr.rms.utils

import android.content.Context

object ResourceManagerUtils {

    // read file from java/main/assets
    fun getAssetFileAsByteArray(context: Context, file_name: String): ByteArray {
        val file = context.assets.open(file_name)
        return file.use {
            file.readBytes()
        }
    }
}