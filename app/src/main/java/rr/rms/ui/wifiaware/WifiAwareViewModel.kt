package rr.rms.ui.wifiaware

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

class WifiAwareViewModel : ViewModel() {

    companion object{
        fun create(activity: FragmentActivity): WifiAwareViewModel{
            return ViewModelProviders.of(activity).get(WifiAwareViewModel::class.java)
        }
    }

}