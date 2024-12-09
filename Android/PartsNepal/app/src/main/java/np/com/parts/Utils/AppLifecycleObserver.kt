package np.com.parts.utils

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

class AppLifecycleObserver(private val context: Context) : DefaultLifecycleObserver {
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App came to foreground
//        CartSyncWorker.cancelSync(context)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App went to background
        Timber.d("App went to background, scheduling cart sync")
//        CartSyncWorker.schedule(context)
    }
} 