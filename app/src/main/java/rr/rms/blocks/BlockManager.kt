package rr.rms.blocks

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.set
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import kotlin.random.Random

object BlockManager{

    private const val CHUNKY_SIZE = 20*1000 // some measurement of size, idk KB

    fun generateBlocks(data: ByteArray, url: String) : Set<Block> {
        val dataList = data.toList()
        return dataList.chunked(CHUNKY_SIZE).map {
            chunk -> Block(
                    generateHash(chunk.toByteArray()),
                    chunk.toByteArray(),
                    url)
        }.toSet()
    }

    fun generateHash(data: ByteArray) : String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(data).toString()
        Timber.d("generated hash %s", hash)
        return hash
    }

    fun blocksToData(blocks: Set<Block>) : ByteArray {
        return blocks.fold(ByteArray(0)){
            sum, element -> sum + element.data
        }
    }

    fun blocksToBitmap(blocks: Set<Block>) : Bitmap {
        val bytes : ByteArray = blocksToData(blocks)
        val bitmap : Bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)  // Assume JPEG byte array
        return bitmap
    }

    // sign image with a dancing bear
    private fun bearhug(bytes : ByteArray) : ByteArray{
        // Assume JPEG byte array
        val bitmap : Bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
        val bitmapcopy = bitmap.copy(bitmap.config,true)

        // randomly jump into img
        val x = Random.nextInt(50, bitmap.width - 50)
        val y = Random.nextInt(50, bitmap.height - 50)

        // stamp image with a signature, currently it draws an L
        // TODO: c'mon we can do better than an L, work on dancing bears implementation
        bitmapcopy[0,0] = Color.BLACK
        for (i in 0..50){
            bitmapcopy[x + i, y] = Color.BLACK
            bitmapcopy[x, y + i] = Color.BLACK
        }

        // save as jpeg byte array (did we lose data when converting to JPEG?)
        val stream = ByteArrayOutputStream()
        bitmapcopy.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun stamp(blocks: Set<Block>) : ByteArray{
        // Get data from chunks of blocks
        val data = blocksToData(blocks)

        // stamp it
        return bearhug(data)
    }
}