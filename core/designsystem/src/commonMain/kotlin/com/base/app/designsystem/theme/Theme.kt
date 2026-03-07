// Theme.kt
package com.hand.log.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

private val DarkColorScheme
	@Composable
	get() = darkColorScheme(
		primary = LightColorScheme.primary,
		onPrimary = LightColorScheme.onPrimary,
		inversePrimary = HmmColor.Gray1,
		secondary = HmmColor.Gray4,
		onSecondary = HmmColor.Gray7,
		background = HmmColor.Gray2,
		onBackground = HmmColor.White2,
		surface = HmmColor.Gray2,
		onSurface = HmmColor.White2,
		onSurfaceVariant = HmmColor.Gray7,
		outline = HmmColor.Gray6,
		outlineVariant = HmmColor.Gray7,
		error = HmmColor.Red1,
		surfaceContainer = HmmColor.Gray1,
		surfaceContainerHigh = HmmColor.Gray3,
		surfaceContainerHighest = HmmColor.Gray5,
	)

private val LightColorScheme
	@Composable
	get() = lightColorScheme(
		primary = HmmColor.Main,
		onPrimary = HmmColor.White1,
		inversePrimary = HmmColor.Main,
		secondary = HmmColor.Gray9,
		onSecondary = HmmColor.Gray5,
		background = HmmColor.White1,
		onBackground = HmmColor.Black,
		surface = HmmColor.White1,
		onSurface = HmmColor.Black,
		onSurfaceVariant = HmmColor.Gray6,
		outline = HmmColor.Gray8,
		outlineVariant = HmmColor.Blue4,
		error = HmmColor.Red3,
		surfaceContainer = HmmColor.Gray11,
		surfaceContainerHigh = HmmColor.Gray10,
		surfaceContainerHighest = HmmColor.Gray8,
	)

val LocalDarkTheme = compositionLocalOf { true }

@Composable
fun HandLogTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

	CompositionLocalProvider(
		LocalDarkTheme provides darkTheme,
		LocalTypography provides Typography,
	) {
		MaterialTheme(
			colorScheme = colorScheme,
			content = content,
		)
	}
}

object HmmTheme {
	val typography: HmmTypography
		@Composable
		get() = LocalTypography.current

	val fixedColor: HmmColorScheme
		@Composable
		get() = LocalHmmColorScheme.current
}
