package rr.rms.chord

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_node.*
import rr.rms.R
import rr.rms.cache.Block
import rr.rms.cache.BlockCache
import rr.rms.cache.BlockCache.ImageCacheListener
import rr.rms.wifiaware.WifiAwareClient
import timber.log.Timber
import java.util.*
import kotlin.concurrent.fixedRateTimer


class NodeActivity : AppCompatActivity() , ImageCacheListener{

    private lateinit var debugTextView: TextView

    private lateinit var cacheTextView: TextView

    private lateinit var addKeyEdit: EditText

    private lateinit var addValueEdit: EditText

    private lateinit var subscribeKeyEdit: EditText

    var syncTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_node)

        debugTextView = findViewById(R.id.debugText)
        addKeyEdit = findViewById(R.id.addDataKey)
        addValueEdit = findViewById(R.id.addDataValue)
        subscribeKeyEdit = findViewById(R.id.subscribeKeyEdit)
        cacheTextView = findViewById(R.id.cacheTextView)
        debugTextView.movementMethod = ScrollingMovementMethod()
        cacheTextView.movementMethod = ScrollingMovementMethod()

        BlockCache.addCacheChangedListener(this)

        addData.setOnClickListener {
            BlockCache.add(addKeyEdit.text.toString(), addValueEdit.text.toString())
        }

        /**
         * Subscribe to other users key list
         */
        subscribeKeyList.setOnClickListener {
            WifiAwareClient.subscribe(this, BlockCache.Keys.KEY_LIST_URL, emptyList(),
                object : WifiAwareClient.WifiAwareCallback {
                    override fun onError(msg: String) {
                        Timber.d("subscribe error %s", msg)
                        debugTextView.append("subscribe error $msg\n")
                    }

                    override fun onSuccess(bytes: ByteArray) {
                        Timber.d("subscribe key list success, received msg %s", String(bytes))
                        debugTextView.append("subscribe key list received: ${String(bytes)}\n")

                        BlockCache.parseAndCacheMetadata(String(bytes))
                    }
                }
            )
        }

        /**
         * Publish our key list
         */
        publishKeyList.setOnClickListener {
            WifiAwareClient.publish(this, BlockCache.Keys.KEY_LIST_URL,
                object : WifiAwareClient.WifiAwareCallback {
                    override fun onError(msg: String) {
                        Timber.d("publish error %s", msg)
                        debugTextView.append("publish error $msg\n")
                    }

                    override fun onSuccess(bytes: ByteArray) {
                        Timber.d("publish key list success, received msg %s", String(bytes))
                        debugTextView.append("publish key list received: ${String(bytes)}\n")
                    }
                },
                BlockCache.getCacheMetaData()
            )
        }

//        /**
//         * Add key to list of keys we want
//         */
//        subscribeKey.setOnClickListener {
//            val wantKey = subscribeKeyEdit.text.toString()
//            BlockCache.addWeWantKey(wantKey)
//        }

        /**
         * Syncs caches between devices
         * Loop:
         *  - publish keys that other nodes request and
         *  - subscribe to keys we need
         */
        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                syncTimer = fixedRateTimer("timer", false, 0L, 7000) {
                    publishKey()
                    subscribeKey()
                }
            } else {
                WifiAwareClient.closeAwareSession()
                syncTimer?.cancel()
            }
        }
    }

    /**
     * 1) Get a key others have asked for
     * 2) Publish the key in form key:value
     * 3)
     */
    fun subscribeKey() {
        if(BlockCache.getKeysWeNeed().isEmpty()){
            Timber.d("no keys we need")
            return
        }

        val keyToSubscribe = BlockCache.getKeysWeNeed().first()
        Timber.d("subscribing key: %s", keyToSubscribe)

        WifiAwareClient.subscribe(
            this, keyToSubscribe, emptyList(),
            object : WifiAwareClient.WifiAwareCallback {
                override fun onError(msg: String) {
                    Timber.d("subscribe error %s", msg)
                    debugTextView.append("subscribe error $msg\n")
                }

                override fun onSuccess(bytes: ByteArray) {
                    Timber.d("subscribe key received msg %s", String(bytes))
                    debugTextView.append("subscribe key received msg: ${String(bytes)}\n")

                    // parse response
                    val (key, value) = String(bytes).split(":")
                    BlockCache.add(key, value)

                    subscribeKey()
                }
            }
        )
    }

    /**
     * 1) Get a key others have asked for
     * 2) Publish the key in form key:value
     * 3)
     */
    fun publishKey() {
        if(BlockCache.getKeysOthersNeed().isEmpty()){
            Timber.d("no keys others want")
            return
        }

        val keyToPublish = BlockCache.getKeysOthersNeed().first()
        Timber.d("publishing key: %s", keyToPublish)

        WifiAwareClient.publish(
            this, keyToPublish,
            object : WifiAwareClient.WifiAwareCallback {
                override fun onError(msg: String) {
                    Timber.d("publish error %s", msg)
                    debugTextView.append("publish error $msg\n")
                }

                override fun onSuccess(bytes: ByteArray) {
                    Timber.d("publish key success, received msg %s", String(bytes))
                    debugTextView.append("publish key received msg: ${String(bytes)}\n")

                    publishKey()
                }
            },
            "${keyToPublish}:${BlockCache.getText(keyToPublish)}"
        )
    }

    override fun onCacheChanged(
        newCache: MutableMap<String, Set<Block>>,
        othersKeys: MutableSet<String>
    ) {
        cacheTextView.text = BlockCache.toString()
    }
}
