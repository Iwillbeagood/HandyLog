package com.hand.log.platform

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

@Composable
actual fun isKeyboardVisible(): Boolean {
	val imeInsets = WindowInsets.ime
	val density = LocalDensity.current
	return imeInsets.getBottom(density) > 0
}
