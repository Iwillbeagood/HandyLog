package com.hand.log.navigation.interop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.hand.log.domain.model.etc.ToastDurationType

val LocalMainActionInterop = staticCompositionLocalOf<MainActionInterop> {
	error("No MainActionInterop provided")
}

interface MainActionInterop {
	fun onFinish()
	fun onRestart()
	fun onShowToast(message: String, toastDurationType: ToastDurationType)
}

@Composable
fun rememberShowSnackBar(): (String) -> Unit {
	val mainActionInterop = LocalMainActionInterop.current
	return { messageType ->
		mainActionInterop.onShowToast(messageType, ToastDurationType.SHORT)
	}
}

@Composable
fun rememberAppFinish(): () -> Unit {
	val mainActionInterop = LocalMainActionInterop.current
	return {
		mainActionInterop.onFinish()
	}
}

@Composable
fun rememberAppRestart(): () -> Unit {
	val mainActionInterop = LocalMainActionInterop.current
	return {
		mainActionInterop.onRestart()
	}
}
