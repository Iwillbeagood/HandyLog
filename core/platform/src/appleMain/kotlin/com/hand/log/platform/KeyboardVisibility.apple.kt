package com.hand.log.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIKeyboardWillShowNotification

@Composable
actual fun isKeyboardVisible(): Boolean {
	var visible by remember { mutableStateOf(false) }

	DisposableEffect(Unit) {
		val center = NSNotificationCenter.defaultCenter

		val showObserver = center.addObserverForName(
			name = UIKeyboardWillShowNotification,
			`object` = null,
			queue = null,
		) { _ ->
			visible = true
		}

		val hideObserver = center.addObserverForName(
			name = UIKeyboardWillHideNotification,
			`object` = null,
			queue = null,
		) { _ ->
			visible = false
		}

		onDispose {
			center.removeObserver(showObserver)
			center.removeObserver(hideObserver)
		}
	}

	return visible
}
