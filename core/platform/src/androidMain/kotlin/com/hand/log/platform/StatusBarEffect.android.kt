package com.hand.log.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
actual fun StatusBarEffect(isDarkTheme: Boolean, backgroundColor: Color) {
	val context = LocalContext.current
	LaunchedEffect(isDarkTheme) {
		val activity = context as? Activity ?: return@LaunchedEffect
		val insetsController = WindowCompat.getInsetsController(
			activity.window,
			activity.window.decorView,
		)
		// isDarkTheme이면 밝은 아이콘(흰색), 라이트면 어두운 아이콘(검정)
		insetsController.isAppearanceLightStatusBars = !isDarkTheme
	}
}
