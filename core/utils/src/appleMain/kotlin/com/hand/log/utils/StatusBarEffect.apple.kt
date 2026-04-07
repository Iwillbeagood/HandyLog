package com.hand.log.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@Composable
actual fun StatusBarEffect(isDarkTheme: Boolean, backgroundColor: Color) {
	LaunchedEffect(isDarkTheme, backgroundColor) {
		NSNotificationCenter.defaultCenter.postNotificationName(
			aName = "UpdateStatusBarStyle" as NSNotificationName,
			`object` = null,
			userInfo = mapOf("isDarkTheme" to isDarkTheme),
		)

		val uiColor = UIColor.colorWithRed(
			red = backgroundColor.red.toDouble(),
			green = backgroundColor.green.toDouble(),
			blue = backgroundColor.blue.toDouble(),
			alpha = backgroundColor.alpha.toDouble(),
		)

		UIApplication.sharedApplication.connectedScenes.forEach { scene ->
			if (scene is UIWindowScene) {
				scene.windows.forEach { window ->
					(window as? UIWindow)?.backgroundColor = uiColor
				}
			}
		}
	}
}
