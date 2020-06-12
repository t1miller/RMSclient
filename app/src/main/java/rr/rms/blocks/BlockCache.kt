package rr.rms.blocks

import android.content.Context
import rr.rms.MainApplication
import rr.rms.ui.wifiaware.NodeDataItem
import rr.rms.utils.ResourceManagerUtils
import timber.log.Timber
import kotlin.random.Random


object BlockCache {

    interface ImageCacheListener {
        fun onCacheChanged(newCache: MutableMap<String, Set<Block>>)
    }

    /** The default user, Grateful Quesadilla */
    private val USER_URL = "user.grateful.quesadilla." + Random.nextInt(0, 1000)

    const val GENERIC_URL = "user.grateful.quesadilla.8008"

    /** Mr. Quesadilla serves orange sunshine */
    private const val DEFAULT_USER_IMAGE = "orange_sunshine.jpg"

    /** Cached data */
    private var cache: MutableMap<String, Set<Block>> = mutableMapOf()

    /** Observables who care, or maybe don't care, when the cache is updated */
    private var cacheChangedListeners = mutableListOf<ImageCacheListener>()

    init {
        addDefaultImage(MainApplication.applicationContext)
    }

    private fun add(newBlocks: Set<Block>, url: String) {
        cache[url] = newBlocks
        notifyListeners()
        Timber.d("updating the cache with %d blocks", newBlocks.size)
        Timber.d("url: %s", url)
    }

    fun add(imgData: ByteArray, url: String) {
        add(BlockManager.generateBlocks(imgData, url), url)
    }

    /**
     * Get default image
     */
    fun getDefaultImage(): ByteArray {
        val defaultImage = cache[USER_URL].orEmpty()
        return BlockManager.blocksToData(defaultImage)
    }

    /**
     * Return cache in a ui listview friendly way.
     */
    fun cacheToNodeData(): List<NodeDataItem> {
        return cache.map { NodeDataItem(it.key, BlockManager.blocksToBitmap(it.value)) }
    }

    /**
     * Add default images to the cache
     */
    private fun addDefaultImage(context: Context) {
        // get defaults from .../assets
        val defaultBlocks = BlockManager.generateBlocks(
            ResourceManagerUtils.getAssetFileAsByteArray(
                context,
                DEFAULT_USER_IMAGE
            ), USER_URL
        )
        val stampedBlocks = BlockManager.stamp(defaultBlocks)
        add(stampedBlocks, USER_URL)
    }

    fun addCacheChangedListener(listener: ImageCacheListener) {
        cacheChangedListeners.add(listener)
    }

    private fun notifyListeners() {
        cacheChangedListeners.onEach {
            it.onCacheChanged(cache)
        }
    }

}