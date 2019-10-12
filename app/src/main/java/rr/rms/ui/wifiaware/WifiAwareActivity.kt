package rr.rms.ui.wifiaware

import Block
import ImageCache
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_wifi_aware_tester.*
import rr.rms.R
import rr.rms.utils.WifiAwareController
import rr.rms.utils.WifiAwareUtils
import timber.log.Timber





class WifiAwareActivity : AppCompatActivity(), ImageCache.ImageCacheListener, NodeRecyclerViewAdapter.NodeListCallback {

    private val PERMISSION_REQUEST_CODE = 101

    /** For recycler view */
    private var recylerAdapter: NodeRecyclerViewAdapter ?= null

    /** This is called when ImageCache is updated */
    override fun onCacheChanged(newCache: MutableMap<String, Set<Block>>) {
        Timber.d("onCacheChanged() called")
        recylerAdapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rr.rms.R.layout.activity_wifi_aware_tester)

        // need permissions for wifi-aware
        setupPermissions()

        Timber.d("Is Wifi Aware available? %s", WifiAwareUtils.isAvailable(applicationContext).toString())

        // register to get notified when the cache changes
        ImageCache.addCacheChangedListener(this)

        // subscribe to listen for other peoples' default image
        WifiAwareController.subscribe(applicationContext, object : WifiAwareController.OnSubscribe{
            override fun msgReceived(msg: ByteArray) {
                // Update our cache with this new image
                ImageCache.updateCache(msg,ImageCache.getUniqueName())
            }
        })

        broadcast_bear_button.setOnClickListener {
            WifiAwareController.broadcast(applicationContext)
        }

        close_session.setOnClickListener {
            WifiAwareController.closeAwareSession()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.list)
        val recyclerViewAdapter =  NodeRecyclerViewAdapter(ImageCache.cacheToNodeData(),this)
        with(recyclerView){
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = recyclerViewAdapter
        }

        status.text = WifiAwareController.toString(this)
    }

    override fun onNodeClicked(item: NodeDataItem?) {
        // blah
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Timber.d("need to ask user for permission")
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // dont care
            }
        }
    }


}
