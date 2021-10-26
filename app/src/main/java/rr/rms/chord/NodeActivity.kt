package rr.rms.chord

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import rr.rms.R
import rr.rms.cache.Block
import rr.rms.cache.BlockCache
import rr.rms.cache.BlockCache.BlockCacheListener
import rr.rms.cache.ClusterCache
import rr.rms.wifiaware.library.WifiAwareClient
import timber.log.Timber
import java.util.*
import kotlin.concurrent.fixedRateTimer


class NodeActivity : AppCompatActivity() , BlockCacheListener, ClusterCache.MetaDataCacheListener{

    private lateinit var debugTextView: TextView
    private lateinit var cacheTextView: TextView
    private lateinit var addKeyEdit: EditText
    private lateinit var addValueEdit: EditText
    private lateinit var subscribeKeyEdit: EditText
    private lateinit var addData: Button
    private lateinit var subscribeKeyList: Button
    private lateinit var publishKeyList: Button
    private lateinit var subscribeOnBoarding: Button
    private lateinit var publishOnBoarding: Button
    private lateinit var syncCache: SwitchCompat

    private var syncTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_node)

        addData = findViewById(R.id.addData)
        subscribeKeyList = findViewById(R.id.subscribeKeyList)
        publishKeyList = findViewById(R.id.publishKeyList)
        subscribeOnBoarding = findViewById(R.id.subscribeOnBoarding)
        publishOnBoarding = findViewById(R.id.publishOnBoarding)
        syncCache = findViewById(R.id.switch1)
        debugTextView = findViewById(R.id.debugText)
        addKeyEdit = findViewById(R.id.addDataKey)
        addValueEdit = findViewById(R.id.addDataValue)
        subscribeKeyEdit = findViewById(R.id.subscribeKeyEdit)
        cacheTextView = findViewById(R.id.cacheTextView)
        debugTextView.movementMethod = ScrollingMovementMethod()
        cacheTextView.movementMethod = ScrollingMovementMethod()

        BlockCache.addCacheChangedListener(this)
        ClusterCache.addListener(this)

//        addData.setOnClickListener {
//            BlockCache.add(addKeyEdit.text.toString(), addValueEdit.text.toString())
//        }
//
//        /**
//         * Subscribe to other users key list
//         */
//        subscribeKeyList.setOnClickListener {
//            WifiAwareClient.subscribe(this, BlockCache.Keys.KEY_LIST_URL, emptyList(), "",
//                object : WifiAwareClient.WifiAwareCallback {
//                    override fun onError(msg: String) {
//                        Timber.d("subscribe error %s", msg)
//                        debugTextView.append("subscribe error $msg\n")
//                    }
//
//                    override fun onSuccess(bytes: ByteArray) {
//                        Timber.d("subscribe key list success, received msg %s", String(bytes))
//                        debugTextView.append("subscribe key list received: ${String(bytes)}\n")
//
//                        BlockCache.parseAndCacheMetadata(String(bytes))
//                    }
//                }
//            )
//        }
//
//        /**
//         * Publish our key list
//         */
//        publishKeyList.setOnClickListener {
//            WifiAwareClient.publish( this,
//                BlockCache.Keys.KEY_LIST_URL,
//                BlockCache.getCacheMetaData(),
//                object : WifiAwareClient.WifiAwareCallback {
//                    override fun onError(msg: String) {
//                        Timber.d("publish error %s", msg)
//                        debugTextView.append("publish error $msg\n")
//                    }
//
//                    override fun onSuccess(bytes: ByteArray) {
//                        Timber.d("publish key list success, received msg %s", String(bytes))
//                        debugTextView.append("publish key list received: ${String(bytes)}\n")
//                    }
//                }
//            )
//        }
//
////        /**
////         * Add key to list of keys we want
////         */
////        subscribeKey.setOnClickListener {
////            val wantKey = subscribeKeyEdit.text.toString()
////            BlockCache.addWeWantKey(wantKey)
////        }
//
//        subscribeOnBoarding.setOnClickListener {
//            WifiAwareClient.subscribe(this,
//                ClusterCache.ON_BOARDING_KEY, emptyList(),
//                "",
//                object : WifiAwareClient.WifiAwareCallback {
//                    override fun onError(msg: String) {
//                        Timber.d("subscribeOnBoarding error %s", msg)
//                        debugTextView.append("subscribeOnBoarding error $msg\n")
//                    }
//
//                    override fun onSuccess(bytes: ByteArray) {
//                        Timber.d("subscribeOnBoarding success, received msg %s", String(bytes))
//                        debugTextView.append("subscribeOnBoarding received: ${String(bytes)}\n")
//                        ClusterCache.parseAndUpdateCache(String(bytes))
//                    }
//                }
//            )
//        }
//
//        /**
//         *  Publish MetaDataCache which includes:
//         *  - my nodeId
//         *  - whether I have internet
//         *  - a list of nodeIds that I've talked to
//         *  onboarding info = "myNodeId:myHasInternet:nodeAId,nodeBId,..."
//         */
//        publishOnBoarding.setOnClickListener {
//            WifiAwareClient.publish(this,
//                ClusterCache.ON_BOARDING_KEY,
//                ClusterCache.toString(),
//                object : WifiAwareClient.WifiAwareCallback {
//                    override fun onError(msg: String) {
//                        Timber.d("publishOnBoarding error %s", msg)
//                        debugTextView.append("publishOnBoarding error $msg\n")
//                    }
//
//                    override fun onSuccess(bytes: ByteArray) {
//                        Timber.d("publishOnBoarding success, received msg %s", String(bytes))
//                        debugTextView.append("publishOnBoarding received: ${String(bytes)}\n")
//                    }
//                }
//            )
//        }
//
//        /**
//         * Syncs caches between devices
//         * Loop:
//         *  - publish keys that other nodes request and
//         *  - subscribe to keys we need
//         */
//        syncCache.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                syncTimer = fixedRateTimer("timer", false, 0L, 7000) {
//                    publishKey()
//                    subscribeKey()
//                }
//            } else {
//                WifiAwareClient.closeAwareSession()
//                syncTimer?.cancel()
//            }
//        }
    }

//    /**
//     * 1) Get a key others have asked for
//     * 2) Publish the key in form key:value
//     * 3)
//     */
//    fun subscribeKey() {
//        if(BlockCache.getKeysWeNeed().isEmpty()){
//            Timber.d("no keys we need")
//            return
//        }
//
//        val keyToSubscribe = BlockCache.getKeysWeNeed().first()
//        Timber.d("subscribing key: %s", keyToSubscribe)
//
//        WifiAwareClient.subscribe(this,
//            keyToSubscribe, emptyList(), "",
//            object : WifiAwareClient.WifiAwareCallback {
//                override fun onError(msg: String) {
//                    Timber.d("subscribe error %s", msg)
//                    debugTextView.append("subscribe error $msg\n")
//                }
//
//                override fun onSuccess(bytes: ByteArray) {
//                    Timber.d("subscribe key received msg %s", String(bytes))
//                    debugTextView.append("subscribe key received msg: ${String(bytes)}\n")
//
//                    // parse response
//                    val (key, value) = String(bytes).split(":")
//                    BlockCache.add(key, value)
//
//                    subscribeKey()
//                }
//            }
//        )
//    }
//
//    /**
//     * 1) Get a key others have asked for
//     * 2) Publish the key in form key:value
//     * 3)
//     */
//    fun publishKey() {
//        if(BlockCache.getKeysOthersNeed().isEmpty()){
//            Timber.d("no keys others want")
//            return
//        }
//
//        val keyToPublish = BlockCache.getKeysOthersNeed().first()
//        Timber.d("publishing key: %s", keyToPublish)
//
//        WifiAwareClient.publish(
//            this,
//            keyToPublish,
//            "${keyToPublish}:${BlockCache.getText(keyToPublish)}",
//            object : WifiAwareClient.WifiAwareCallback {
//                override fun onError(msg: String) {
//                    Timber.d("publish error %s", msg)
//                    debugTextView.append("publish error $msg\n")
//                }
//
//                override fun onSuccess(bytes: ByteArray) {
//                    Timber.d("publish key success, received msg %s", String(bytes))
//                    debugTextView.append("publish key received msg: ${String(bytes)}\n")
//                    publishKey()
//                }
//            })
//    }

    override fun onCacheChanged(
        newCache: MutableMap<String, Set<Block>>,
        othersKeys: MutableSet<String>
    ) {
        cacheTextView.text = "$BlockCache\n$ClusterCache"
    }

    override fun onCacheChanged(newCache: MutableMap<String, Node>) {
        cacheTextView.text = "$BlockCache\n$ClusterCache"
    }
}
