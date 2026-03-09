package com.hand.log.toast

expect open class ToastManager() {
	fun showToast(message: String, toastDurationType: ToastDurationType)
}
