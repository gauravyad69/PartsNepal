package np.com.parts

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import me.ibrahimsn.lib.BuildConfig
import np.com.parts.API.NetworkModule
import np.com.parts.database.AppDatabase
import np.com.parts.repository.CartRepository
import np.com.parts.utils.AppLifecycleObserver
import timber.log.Timber

class App : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val cartRepository: CartRepository by lazy { CartRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // init timber
        NetworkModule.initialize(this)

            Timber.plant(Timber.DebugTree())
            Timber.tag("partezNepal")

            ProcessLifecycleOwner.get().lifecycle.addObserver(
                AppLifecycleObserver(this)
            )

    }
}
