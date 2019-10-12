//package rr.rms.ui.roundrobintest
//
//import android.graphics.BitmapFactory
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import timber.log.Timber
//
//
///** Important data structures */
//class RoundRobinViewModel : ViewModel() {
//
//    val uiListData = MutableLiveData<MutableList<NodeDataItem>>()
//    init {
//        uiListData.value = mutableListOf()
//    }
//
//    fun updateLiveUIData(imgData: ByteArray, peerId: String) {
//        val bmp = BitmapFactory.decodeByteArray(imgData, 0, imgData.size)
//        val nodeItem = NodeDataItem(peerId,bmp)
//
////        // get old list
////        var oldList = uiListData.value
////        if(oldList == null){
////            oldList = mutableListOf()
////        }
////        oldList.add(nodeItem)
//
////        val oldList = uiListData.value.amutableListOf(nodeItem)
//        uiListData.postValue()
//        uiListData.setValue(oldList)
//
//
//        // add value
////
////        // notify everyone of new value
////
////        uiListData.postValue()
////        uiListData..add())
//        Timber.d("LiveData updated: %s" , uiListData.value)
////        uiListData.value = uiListData.value  // Post change
//    }
//
////    fun fetchImages(){
////        // TODO: Get some abstraction of a network device and perform a network call
////        // TODO: Update some data structure with images and notify listeners that there is new data
////    }
//
//}
