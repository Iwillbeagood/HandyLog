package com.hand.log.platform.toast

import android.widget.Toast
import com.hand.log.domain.model.etc.ToastDurationType
import com.hand.log.platform.activityProvider

actual open class ToastManager actual constructor() {
	actual fun showToast(message: String, toastDurationType: ToastDurationType) {
		val context = activityProvider.invoke()
		val duration = when (toastDurationType) {
			ToastDurationType.SHORT -> Toast.LENGTH_SHORT
			ToastDurationType.LONG -> Toast.LENGTH_LONG
		}
		Toast.makeText(context, message, duration).show()
	}
}
