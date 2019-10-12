package rr.rms

import android.app.Application
import android.content.Context

/**
 *  This class enables global access to application context
 */
class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        lateinit var instance: MainApplication
            private set

        val applicationContext: Context get() { return instance.applicationContext }
    }
}