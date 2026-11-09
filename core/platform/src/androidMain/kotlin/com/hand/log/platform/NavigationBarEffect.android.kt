package com.hand.log.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
actual fun NavigationBarEffect(backgroundColor: Color) {
	val context = LocalContext.current
	val isLightBackground = backgroundColor.luminance() > 0.5f
	LaunchedEffect(isLightBackground) {
		val activity = context as? Activity ?: return@LaunchedEffect
		WindowCompat.getInsetsController(
			activity.window,
			activity.window.decorView,
		).isAppearanceLightNavigationBars = isLightBackground
	}
}
