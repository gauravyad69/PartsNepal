package np.com.parts

import android.app.Application
import me.ibrahimsn.lib.BuildConfig
import np.com.parts.API.NetworkModule
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // init timber
        NetworkModule.initialize(this)

            Timber.plant(Timber.DebugTree())
            Timber.tag("partezNepal")

    }
}
