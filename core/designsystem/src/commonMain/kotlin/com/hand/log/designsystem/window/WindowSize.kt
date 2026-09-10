package com.hand.log.designsystem.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass { Compact, Medium, Expanded }

data class WindowSize(
	val widthClass: WindowWidthClass,
	val widthDp: Dp,
) {
	val isCompact: Boolean get() = widthClass == WindowWidthClass.Compact
	val isMedium: Boolean get() = widthClass == WindowWidthClass.Medium
	val isExpanded: Boolean get() = widthClass == WindowWidthClass.Expanded
	val isLarge: Boolean get() = widthClass != WindowWidthClass.Compact
}

val LocalWindowSize = compositionLocalOf { WindowSize(WindowWidthClass.Compact, 0.dp) }

@Composable
fun rememberWindowSize(): WindowSize {
	val windowInfo = LocalWindowInfo.current
	val density = LocalDensity.current
	val containerSize = windowInfo.containerSize
	return remember(containerSize, density) {
		val widthDp = with(density) { containerSize.width.toDp() }
		WindowSize(widthClassOf(widthDp), widthDp)
	}
}

@Composable
fun ProvideWindowSize(content: @Composable () -> Unit) {
	CompositionLocalProvider(LocalWindowSize provides rememberWindowSize(), content = content)
}

private fun widthClassOf(widthDp: Dp): WindowWidthClass = when {
	widthDp < 600.dp -> WindowWidthClass.Compact
	widthDp < 840.dp -> WindowWidthClass.Medium
	else -> WindowWidthClass.Expanded
}
