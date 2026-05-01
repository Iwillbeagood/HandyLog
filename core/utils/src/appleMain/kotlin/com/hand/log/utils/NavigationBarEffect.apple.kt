package com.hand.log.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName

@Composable
actual fun NavigationBarEffect(backgroundColor: Color) {
	LaunchedEffect(backgroundColor) {
		NSNotificationCenter.defaultCenter.postNotificationName(
			aName = "UpdateNavigationBarStyle" as NSNotificationName,
			`object` = null,
			userInfo = mapOf(
				"red" to backgroundColor.red.toDouble(),
				"green" to backgroundColor.green.toDouble(),
				"blue" to backgroundColor.blue.toDouble(),
				"alpha" to backgroundColor.alpha.toDouble(),
			),
		)
	}
}
