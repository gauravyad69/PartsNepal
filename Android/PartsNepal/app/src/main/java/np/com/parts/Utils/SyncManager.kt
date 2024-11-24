package np.com.parts.utils

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SyncManager {
    fun getSyncStatus(context: Context): Flow<SyncStatus> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("cart_sync_worker")
            .map { workInfoList ->
                when {
                    workInfoList.isEmpty() -> SyncStatus.IDLE
                    workInfoList.any { it.state == WorkInfo.State.RUNNING } -> SyncStatus.SYNCING
                    workInfoList.any { it.state == WorkInfo.State.FAILED } -> SyncStatus.FAILED
                    workInfoList.any { it.state == WorkInfo.State.SUCCEEDED } -> SyncStatus.SUCCESS
                    else -> SyncStatus.IDLE
                }
            }
    }
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED
} 