
import android.content.Context
import rr.rms.MainApplication
import rr.rms.ui.wifiaware.NodeDataItem
import rr.rms.utils.ResourceManagerUtils
import timber.log.Timber
import kotlin.random.Random


object ImageCache {

    interface ImageCacheListener {
        fun onCacheChanged(newCache: MutableMap<String, Set<Block>>)
    }

    /** The default user, Grateful Quesadilla */
    const val DEFAULT_URL = "user.grateful.quesadilla"

    /** Mr. Quesadilla serves orange sunshine */
     const val DEFAULT_IMAGE_NAME = "orange_sunshine.jpg"

    /** Cached data */
    private var cache: MutableMap<String, Set<Block>> = mutableMapOf()

    /** Observables who care, or maybe don't care, when the cache is updated */
    private var cacheChangedListeners = mutableListOf<ImageCacheListener>()

    init {
        // initialize the cache with a default image
        addDefaultImage(MainApplication.applicationContext)
    }

    /**  */
    fun updateCache(newBlocks: Set<Block>, url: String) {
        cache[url] = newBlocks
        notifyListeners()
        Timber.d("updating the cache with %d blocks", newBlocks.size)
        Timber.d("url: %s", url)
    }

    /***/
    fun updateCache(imgData: ByteArray, url: String) {
        updateCache(BlockManager.generateBlocks(imgData, url), url)
    }

    /**
     * Get default image
     */
    fun getDefaultImage(): ByteArray {
        val defaultImage = cache[DEFAULT_URL].orEmpty()
        return BlockManager.blocksToData(defaultImage)
    }

    fun getUniqueName(): String {
        return DEFAULT_URL + Random.nextInt(0, 100)
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
    fun addDefaultImage(context: Context) {
        // get defaults from .../assets
        val defaultBlocks = BlockManager.generateBlocks(
            ResourceManagerUtils.getAssetFileAsByteArray(
                context,
                DEFAULT_IMAGE_NAME
            ), DEFAULT_URL
        )
        val stampedBlocks = BlockManager.stamp(defaultBlocks)
        updateCache(stampedBlocks, DEFAULT_URL)
    }

    fun addCacheChangedListener(listener: ImageCache.ImageCacheListener) {
        cacheChangedListeners.add(listener)
    }

    private fun notifyListeners() {
        cacheChangedListeners.onEach {
            it.onCacheChanged(cache)
        }
    }

}