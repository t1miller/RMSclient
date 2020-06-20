package rr.rms.wifiaware


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_wifi_aware_tester.*
import rr.rms.R
import rr.rms.cache.Block
import rr.rms.cache.BlockCache
import rr.rms.cache.BlockCache.Keys.GENERIC_URL
import rr.rms.ui.wifiaware.NodeDataItem
import rr.rms.ui.wifiaware.NodeRecyclerViewAdapter
import rr.rms.utils.NetworkUtils
import timber.log.Timber
import java.net.Socket


class WifiAwareActivity : AppCompatActivity(),
    BlockCache.ImageCacheListener,
    NodeRecyclerViewAdapter.NodeListCallback {

    private val PERMISSION_REQUEST_CODE = 101

    var discoverySession: PublishDiscoverySession? = null

    var subscribeSession: SubscribeDiscoverySession? = null

    var session: WifiAwareSession? = null

    var mPeerHandle: PeerHandle? = null

    var mSocket: Socket? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            appendToLog("Wifi Aware is ${WifiAwareUtils.isAvailable(context)}")
        }
    }

    private var recylerAdapter: NodeRecyclerViewAdapter? = null

    override fun onCacheChanged(
        newCache: MutableMap<String, Set<Block>>,
        keysWeWant: MutableSet<String>
    ) {
        appendToLog("onCacheChanged() called")
        recylerAdapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_aware_tester)

        // permission check
        setupPermissions()

        // register to get notified when the cache changes
        BlockCache.addCacheChangedListener(this)

        broadcast_button.setOnClickListener {
            publish(GENERIC_URL)
        }

//        close_button.setOnClickListener {
//            WifiAwareController.closeAwareSession()
//        }

        subscribe_button.setOnClickListener {
//            subscribe(GENERIC_URL)
        }

        msg_button.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_LONG).show()
        }

        receive_bear_button.setOnClickListener {
            receiveBear()
        }

        send_bear_button.setOnClickListener {
            sendBear()
        }


        val wifiAwareManager = getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        wifiAwareManager?.attach(object : AttachCallback() {
            override fun onAttached(wifiAwareSession: WifiAwareSession?) {
                super.onAttached(wifiAwareSession)
                Timber.d("onAttach() WifiAwareManager succeed")
                session = wifiAwareSession
            }

            override fun onAttachFailed() {
                super.onAttachFailed()
                Timber.d("onAttachFailed() WifiAwareManager failed")
            }

        }, null)


        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recylerAdapter = NodeRecyclerViewAdapter(BlockCache.cacheToNodeData(), this)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = recylerAdapter
        }

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
//        WifiAwareController.closeAwareSession()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED))
    }

    override fun onNodeClicked(item: NodeDataItem?) {
        // blah
    }

    private fun setupPermissions() {
        val permissionFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionFine != PackageManager.PERMISSION_GRANTED){
            Timber.d("need to ask user for permission")
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // dont care
            }
        }
    }

    fun appendToLog(msg: String) {
        Timber.d("$msg\n")
        log_textview.append(msg + "\n")
    }


    /**
     *  Broadcast the default image in our cache. If a subscriber sees
     *  this publish and sends a message. We respond with a bear image.
     */
    private fun publish(url: String) {
        appendToLog("Publishing a bear at $url")
        session?.publish(
            WifiAwareUtils.generatePublishConfig(url),
            object : DiscoverySessionCallback() {

                override fun onPublishStarted(session: PublishDiscoverySession) {
                    discoverySession = session
                    appendToLog("onPublishStarted()")
                }

                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                    mPeerHandle = peerHandle
                    appendToLog("onMessageReceived()")
                }

            },
            null
        )
    }


//    /**
//     *  Subscribe to ask for other peoples' bear images.
//     */
//    private fun subscribe(url: String) {
//        appendToLog("Subscribing to $url")
//        session?.subscribe(
//            WifiAwareUtils.generateSubscribeConfig(url),
//            object : DiscoverySessionCallback() {
//
//                override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
//                    appendToLog("Subscriber sent msg: $message \nSending them a bear.")
//                }
//
//                override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
//                    subscribeSession = session
//                    appendToLog("onSubscribeStarted()")
//                }
//
//                override fun onServiceDiscovered(
//                    peerHandle: PeerHandle,
//                    serviceSpecificInfo: ByteArray,
//                    matchFilter: List<ByteArray>
//                ) {
//                    val serviceName = serviceSpecificInfo.toString()
//                    val msgToSend = "Give me your bear".toByteArray()
//
//                    subscribeSession?.sendMessage(peerHandle, 0, msgToSend)
//                    appendToLog("onServiceDiscovered():\n\tservice name: $serviceName\n\tsending: ${String(msgToSend)}")
//                }
//
//            },
//            null
//        )
//    }

    private fun makeNetworkRequest() {
        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                appendToLog("network onAvailable()")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                appendToLog("network onCapabilitiesChanged()")

                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port
                mSocket = network.getSocketFactory().createSocket(peerIpv6, peerPort)
            }

            override fun onLost(network: Network) {
                appendToLog("network onLost()")
            }
        }

        // TODO: unregister
        val connectManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = buildNetworkRequest()
        if(networkRequest != null){
            connectManager.requestNetwork(networkRequest, callback)
        }

    }

    private fun sendBear(){
        makeNetworkRequest()
        mSocket?.apply {
            val image = BlockCache.getDefaultImage()
            NetworkUtils.sendBytes(this, image)
            appendToLog("sending image to neighbor")
        }
    }

    private fun receiveBear(){
        makeNetworkRequest()
        mSocket?.apply {
            NetworkUtils.receiveBytes(this)?.apply {
                BlockCache.add("blah", this)
                appendToLog("added neighbors image to cache")
            }
        }
    }

    private fun buildNetworkRequest() : NetworkRequest?{
        if(discoverySession == null){
            appendToLog("discovery session null")
            return null
        }
        if(mPeerHandle == null){
            appendToLog("peer handle null")
            return null
        }
        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(requireNotNull(discoverySession), requireNotNull(mPeerHandle))
            .setPskPassphrase("somePassword")
            .setPort(WifiAwareUtils.getAPort())
            .build()
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()
    }

}
