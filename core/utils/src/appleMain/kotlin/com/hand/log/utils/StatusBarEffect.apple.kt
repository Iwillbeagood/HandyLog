package com.hand.log.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.setStatusBarStyle

@Composable
actual fun StatusBarEffect(isDarkTheme: Boolean, backgroundColor: Color) {
	LaunchedEffect(isDarkTheme, backgroundColor) {
		val style = if (isDarkTheme) {
			UIStatusBarStyleLightContent
		} else {
			UIStatusBarStyleDarkContent
		}
		UIApplication.sharedApplication.setStatusBarStyle(style, animated = true)

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
