package com.hand.log.platform.toast

import com.hand.log.domain.model.etc.ToastDurationType

expect open class ToastManager() {
	fun showToast(message: String, toastDurationType: ToastDurationType)
}
