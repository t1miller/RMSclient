package rr.rms.cache

import rr.rms.chord.Node
import timber.log.Timber


object MetaDataCache {

    interface MetaDataCacheListener {
        fun onCacheChanged(newCache: MutableMap<String, Node>)
    }

    const val ON_BOARDING_KEY = "onBoarding"

    private val nodeMetadataCache = mutableMapOf<String, Node>()

    private val cacheChangedListeners = mutableListOf<MetaDataCacheListener>()

    private val me: Node? = Node()

    fun add(id: String, node: Node?) {
        if(node == null){
            return
        }
        nodeMetadataCache[id] = node
        notifyListeners()
    }

    fun addListener(listener: MetaDataCacheListener) {
        cacheChangedListeners.add(listener)
    }
//    /**
//     * @return True if date1 is bigger and False if less or equal
//     */
//    private fun compareDates(date1: String?, date2: String?) : Boolean{
//        if (date2 == null) {
//            return true
//        }
//        if (date1 == null) {
//            return false
//        }
//        return date1.toInt() > date2.toInt()
//    }
//
//    /**
//     * The ISO date formatter that formats or parses a date without an
//     * offset, such as '20111203'.
//     * @return the date
//     */
//    fun getDate() : String{
//        val current = LocalDateTime.now()
//        val formatter = DateTimeFormatter.BASIC_ISO_DATE
//        return current.format(formatter)
//    }

    private fun notifyListeners() {
        cacheChangedListeners.onEach {
            it.onCacheChanged(nodeMetadataCache)
        }
    }

//    fun toPrettyString() : String {
//        var text = "MetaDataCache\n\n"
//        nodeMetadataCache.forEach { key, value ->
//            text += "key: $key value"
//        }
//    }

    override fun toString() : String {
        var text = me.toString() + ":"
        text += nodeMetadataCache.keys.joinToString(separator = ",")
        Timber.d("MetaDataCache toString: %s", text)
        return text
    }

    fun parseAndUpdateCache(text: String) {
        val (hasInternet, nodeId, nodeIds) = text.split(":")
        var listNodeIds = nodeIds.split(",")
        val boolHasInternet = (hasInternet == "1")

        if(nodeMetadataCache[nodeId] == null){
            add(nodeId, Node(boolHasInternet, nodeId.toLong()))
            Timber.d("nodeMetadataCache adding node to cache %s", nodeMetadataCache[nodeId].toString())
        } else {
            val existingNode = nodeMetadataCache[nodeId]
            existingNode?.hasInternet = boolHasInternet
            add(nodeId, existingNode)
            Timber.d("nodeMetadataCache updating existing cache %s", nodeMetadataCache[nodeId].toString())
        }

        listNodeIds = listNodeIds.filter { item -> (item) != "" }
        for (id in listNodeIds){
            Timber.d("id = %s", id)
            val oNode = nodeMetadataCache[id]
            if (oNode == null) {
                add(id, Node(null, id.toLong()))
            }
        }
    }
}


