package np.com.parts.utils

import android.content.Context

class DialogManager(context: Context) {
    private val loadingDialog = LoadingDialog(context)
    private val errorDialog = ErrorDialog(context)

    fun showLoading(message: String = "Loading...") {
        errorDialog.dismiss()
        loadingDialog.show(message)
    }

    fun showError(
        title: String = "Error",
        message: String,
        showRetry: Boolean = true,
        onRetry: (() -> Unit)? = null
    ) {
        loadingDialog.dismiss()
        errorDialog.show(title, message, showRetry, onRetry)
    }

    fun dismissAll() {
        loadingDialog.dismiss()
        errorDialog.dismiss()
    }
} 