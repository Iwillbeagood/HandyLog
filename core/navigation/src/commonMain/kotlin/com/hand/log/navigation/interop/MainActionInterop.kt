package com.hand.log.navigation.interop

import androidx.compose.runtime.staticCompositionLocalOf
import com.hand.log.domain.model.etc.ToastDurationType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

val LocalMainActionInterop = staticCompositionLocalOf<MainActionInterop> {
	error("No MainActionInterop provided")
}

interface MainActionInterop {
	fun onFinish()
	fun onRestart()
	fun onShowToast(message: String, toastDurationType: ToastDurationType = ToastDurationType.SHORT)
	suspend fun onShowToast(res: StringResource, toastDurationType: ToastDurationType = ToastDurationType.SHORT) {
		val message = getString(res)
		onShowToast(message, toastDurationType)
	}
}
