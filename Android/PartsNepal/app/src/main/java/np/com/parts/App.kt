package np.com.parts

import android.app.Application
import me.ibrahimsn.lib.BuildConfig
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // init timber
            Timber.plant(Timber.DebugTree())
            Timber.tag("partezNepal")

    }
}
