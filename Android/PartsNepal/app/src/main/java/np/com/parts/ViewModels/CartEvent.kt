sealed class CartEvent {
    data class ShowMessage(val message: String) : CartEvent()
    data class ItemAdded(val name: String) : CartEvent()
    data object ItemRemoved : CartEvent()
    data object QuantityUpdated : CartEvent()
    data object SyncStarted : CartEvent()
    data object SyncCompleted : CartEvent()
    data class SyncFailed(val error: String) : CartEvent()
} 