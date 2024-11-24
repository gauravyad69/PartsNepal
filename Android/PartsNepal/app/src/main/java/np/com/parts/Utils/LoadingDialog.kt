package np.com.parts.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import np.com.parts.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) {
    private var dialog: Dialog = Dialog(context)
    private var binding: DialogLoadingBinding

    init {
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun show(message: String = "Loading...") {
        binding.loadingText.text = message
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