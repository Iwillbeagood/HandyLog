// Theme.kt
package com.base.app.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme
	@Composable
	get() = darkColorScheme(
		primary = LightColorScheme.primary,
		onPrimary = LightColorScheme.onPrimary,
		inversePrimary = MainColor.Gray1,
		secondary = MainColor.Gray4,
		onSecondary = MainColor.Gray7,
		background = MainColor.Gray2,
		onBackground = MainColor.White2,
		surface = MainColor.Gray2,
		onSurface = MainColor.White2,
		onSurfaceVariant = MainColor.Gray7,
		outline = MainColor.Gray6,
		outlineVariant = MainColor.Gray7,
		error = MainColor.Red1,
		surfaceContainer = MainColor.Gray1,
		surfaceContainerHigh = MainColor.Gray3,
		surfaceContainerHighest = MainColor.Gray5,
	)

private val LightColorScheme
	@Composable
	get() = lightColorScheme(
		primary = MainColor.Main,
		onPrimary = MainColor.White1,
		inversePrimary = MainColor.Main,
		secondary = MainColor.Gray9,
		onSecondary = MainColor.Gray5,
		background = MainColor.White1,
		onBackground = MainColor.Black,
		surface = MainColor.White1,
		onSurface = MainColor.Black,
		onSurfaceVariant = MainColor.Gray6,
		outline = MainColor.Gray8,
		outlineVariant = MainColor.Blue4,
		error = MainColor.Red3,
		surfaceContainer = MainColor.Gray11,
		surfaceContainerHigh = MainColor.Gray10,
		surfaceContainerHighest = MainColor.Gray8,
	)

val LocalDarkTheme = compositionLocalOf { true }

@Composable
fun HandLogTheme(
	fontFamily: FontFamily = FontFamily.Default,
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

	CompositionLocalProvider(
		LocalDarkTheme provides darkTheme,
		LocalTypography provides HandyTypography.with(fontFamily),
	) {
		MaterialTheme(
			colorScheme = colorScheme,
			content = content,
		)
	}
}

object HandyTheme {
	val typography: HandyTypography
		@Composable
		get() = LocalTypography.current

	val fixedColor: MainColorScheme
		@Composable
		get() = LocalHmmColorScheme.current
}
