package rr.rms.cache

import android.content.Context
import rr.rms.cache.BlockCache.Keys.DEFAULT_USER_IMAGE
import rr.rms.cache.BlockCache.Keys.USER_URL
import rr.rms.ui.wifiaware.NodeDataItem
import rr.rms.utils.ResourceManagerUtils
import timber.log.Timber
import kotlin.random.Random


object BlockCache {

    interface ImageCacheListener {
        fun onCacheChanged(newCache: MutableMap<String, Set<Block>>,
                           othersKeys: MutableSet<String>)
    }

    object Keys {

        const val KEY_LIST_URL = "keyList"

        const val GENERIC_URL = "user.grateful.quesadilla.8008"

        /** Mr. Quesadilla serves orange sunshine */
        const val DEFAULT_USER_IMAGE = "orange_sunshine.jpg"

        /** The default user, Grateful Quesadilla */
        val USER_URL = "user.grateful.quesadilla." + Random.nextInt(0, 1000)
    }

    /** Cached data */
    private var cache: MutableMap<String, Set<Block>> = mutableMapOf()

    /** Keys others have */
    private var othersKeys: MutableSet<String> = mutableSetOf()

    /** Observables who care, or maybe don't care, when the cache is updated */
    private var cacheChangedListeners = mutableListOf<ImageCacheListener>()

    init {
        addRandomKeysToCache()
    }

    private fun add(key: String, newBlocks: Set<Block>) {
        cache[key] = newBlocks
        notifyListeners()
        Timber.d("updating the cache with %d blocks", newBlocks.size)
        Timber.d("key: %s", key)
    }

    fun add(key: String, imgData: ByteArray) {
        add(key, BlockUtils.generateBlocks(imgData, key))
    }

    fun add(key: String, value: String) {
        add(key,BlockUtils.generateBlocks(value.toByteArray(), key))
    }

    fun get(key: String) : Set<Block>? {
        return cache[key]
    }

    fun getText(key: String) : String? {
        return get(key).toText()
    }

    /**
     * Get default image
     */
    fun getDefaultImage(): ByteArray {
        val defaultImage = cache[USER_URL].orEmpty()
        return BlockUtils.blocksToData(defaultImage)
    }

    /**
     * Return cache in a ui listview friendly way.
     */
    fun cacheToNodeData(): List<NodeDataItem> {
        return cache.map { NodeDataItem(it.key, BlockUtils.blocksToBitmap(it.value)) }
    }

    /**
     * Add default images to the cache
     */
    private fun addDefaultImage(context: Context) {
        // get defaults from .../assets
        val defaultBlocks = BlockUtils.generateBlocks(
            ResourceManagerUtils.getAssetFileAsByteArray(
                context,
                DEFAULT_USER_IMAGE
            ), USER_URL
        )
        val stampedBlocks = BlockUtils.stamp(defaultBlocks)
        add(USER_URL, stampedBlocks)
    }

    fun addCacheChangedListener(listener: ImageCacheListener) {
        cacheChangedListeners.add(listener)
    }

    private fun notifyListeners() {
        cacheChangedListeners.onEach {
            it.onCacheChanged(cache, othersKeys)
        }
    }

    /**
     * Get cached keys
     * @return all keys stored in this cache
     */
    fun getCacheMetaData() : String {
        return cache.keys.joinToString { "$it@" } + ":" + othersKeys.joinToString { "$it@" }
    }

    fun parseAndCacheMetadata(rawdata: String){
        // parse data
        othersKeys.addAll(rawdata.split("@"))
        notifyListeners()
    }

    fun getCachedKeys() : Set<String> {
        return cache.keys
    }

    fun getKeysWeNeed() : Set<String> {
        return othersKeys.minus(cache.keys)
    }

    fun getKeysOthersNeed() : Set<String> {
        return cache.keys.minus(othersKeys)
    }

    /**
     * Extension to get String representation of blocks
     */
    private fun Set<Block>?.toText(): String? {
        return this?.fold("",{ acc, value ->
            acc + String(value.data)
        })
    }

    private fun addRandomKeysToCache() {
        val keyValues = mapOf(
            "Lamb" to "says woof",
            "Clyde" to "says hello",
            "Kate" to "says hi",
            "Trent" to "says hey"
        ).toList()

        for(i in 0..2) {
            val randIdx = Random.nextInt(0,keyValues.size)
            val (key, value) = keyValues[randIdx]
            add(key,value)
        }
    }

    override fun toString(): String{
        var text = "Key-Value(s) Cached:\n"
        cache.forEach { (key, value) ->
            text += "key: $key value: ${value.toText()}\n"
        }

        text += "\nKey(s) others have:\n"
        othersKeys.forEach { key ->
            text += "key: $key\n"
        }

        return text
    }
}