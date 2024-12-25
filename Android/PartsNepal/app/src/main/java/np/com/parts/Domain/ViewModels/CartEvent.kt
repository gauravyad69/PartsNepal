sealed interface CartEvent {
    data class ShowMessage(val message: String) : CartEvent
    data class ItemAdded(val name: String) : CartEvent
    data object ItemRemoved : CartEvent
    data object QuantityUpdated : CartEvent
    sealed interface SyncEvent : CartEvent {
        data object Started : SyncEvent
        data object Completed : SyncEvent
        data class Failed(val error: String) : SyncEvent
    }
} 