package np.com.parts.workers

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.App
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CartSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val cartRepository = (context.applicationContext as App).cartRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting cart sync")
            
            val syncResult = cartRepository.syncCart()
            
            return@withContext if (syncResult.isSuccess) {
                Timber.d("Cart sync completed successfully")
                Result.success()
            } else {
                Timber.e("Cart sync failed: ${syncResult.exceptionOrNull()?.message}")
                // Retry with backoff if sync fails
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during cart sync")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "cart_sync_worker"
        private const val SYNC_INTERVAL_MINUTES = 15L

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // One-time work request for immediate sync
            val immediateSync = OneTimeWorkRequestBuilder<CartSyncWorker>()
                .setConstraints(constraints)
                .build()

            // Periodic work request for background sync
            val periodicSync = PeriodicWorkRequestBuilder<CartSyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).apply {
                // Enqueue immediate sync
                enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    immediateSync
                )

                // Enqueue periodic sync
                enqueueUniquePeriodicWork(
                    "${WORK_NAME}_periodic",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicSync
                )
            }
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(WORK_NAME)
                cancelUniqueWork("${WORK_NAME}_periodic")
            }
        }
    }
} 