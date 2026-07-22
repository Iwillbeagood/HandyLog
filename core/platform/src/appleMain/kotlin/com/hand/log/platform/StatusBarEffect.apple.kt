package com.hand.log.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName

@Composable
actual fun StatusBarEffect(isDarkTheme: Boolean, backgroundColor: Color) {
	LaunchedEffect(isDarkTheme, backgroundColor) {
		NSNotificationCenter.defaultCenter.postNotificationName(
			aName = "UpdateStatusBarStyle" as NSNotificationName,
			`object` = null,
			userInfo = mapOf(
				"isDarkTheme" to isDarkTheme,
				"red" to backgroundColor.red.toDouble(),
				"green" to backgroundColor.green.toDouble(),
				"blue" to backgroundColor.blue.toDouble(),
				"alpha" to backgroundColor.alpha.toDouble(),
			),
		)
	}
}
