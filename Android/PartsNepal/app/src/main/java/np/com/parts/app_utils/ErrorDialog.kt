package np.com.parts.app_utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import np.com.parts.databinding.DialogErrorBinding

class ErrorDialog(context: Context) {
    private var dialog: Dialog = Dialog(context)
    private var binding: DialogErrorBinding
    private var onRetryClick: (() -> Unit)? = null

    init {
        binding = DialogErrorBinding.inflate(LayoutInflater.from(context))
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.dismissButton.setOnClickListener {
            dismiss()
        }

        binding.retryButton.setOnClickListener {
            dismiss()
            onRetryClick?.invoke()
        }
    }

    fun show(
        title: String = "Error",
        message: String,
        showRetry: Boolean = true,
        onRetry: (() -> Unit)? = null
    ) {
        binding.apply {
            errorTitleText.text = title
            errorMessageText.text = message
            retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
            this@ErrorDialog.onRetryClick = onRetry
        }

        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun isShowing(): Boolean = dialog.isShowing
} 