// Theme.kt
package com.hand.log.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme
	get() = darkColorScheme(
		primary = Color(0xFF0FB67F),              // primary
		onPrimary = Color(0xFFF2F2F2),            // onPrimary
		secondary = Color(0xFF2F3541),            // secondary
		onSecondary = Color(0xFFF2F2F2),          // onSecondary
		tertiary = Color(0xFF22C38D),             // accent
		onTertiary = Color(0xFFF2F2F2),           // onAccent
		background = Color(0xFF14171E),           // background
		onBackground = Color(0xFFF2F2F2),         // textPrimary
		surface = Color(0xFF14171E),              // background
		onSurface = Color(0xFFF2F2F2),            // textPrimary
		surfaceVariant = Color(0xFF272B34),       // muted
		onSurfaceVariant = Color(0xFF808897),     // textSecondary
		surfaceContainer = Color(0xFF1D212A),     // card
		surfaceContainerHigh = Color(0xFF191C24), // modalBackground
		outline = Color(0xFF30353F),              // border
		outlineVariant = Color(0xFF2B303A),       // inputBorder
		error = Color(0xFFDC2828),                // error
	)

private val LightColorScheme
	get() = lightColorScheme(
		primary = Color(0xFF0EA875),              // primary
		onPrimary = Color(0xFFFFFFFF),            // onPrimary
		secondary = Color(0xFFE7E9ED),            // secondary
		onSecondary = Color(0xFF353C49),          // onSecondary
		tertiary = Color(0xFF1EAD7D),             // accent
		onTertiary = Color(0xFFFFFFFF),           // onAccent
		background = Color(0xFFF9F9F9),           // background
		onBackground = Color(0xFF181C24),         // textPrimary
		surface = Color(0xFFF9F9F9),              // background
		onSurface = Color(0xFF181C24),            // textPrimary
		surfaceVariant = Color(0xFFF0F1F4),       // muted
		onSurfaceVariant = Color(0xFF676E7E),     // textSecondary
		surfaceContainer = Color(0xFFFFFFFF),     // card
		surfaceContainerHigh = Color(0xFFFFFFFF), // modalBackground
		outline = Color(0xFFDCDEE4),              // border
		outlineVariant = Color(0xFFE1E4E9),       // inputBorder
		error = Color(0xFFDC2828),                // error
	)

val LocalDarkTheme = compositionLocalOf { true }

@Composable
fun HandLogTheme(
	fontFamily: FontFamily = FontFamily.Default,
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
	val handyColorScheme = if (darkTheme) {
		DarkHandyColorScheme
	} else {
		HandyColorScheme(
			background = Color(0xFFF9F9F9),
			textPrimary = Color(0xFF181C24),
			card = Color(0xFFFFFFFF),
			modalBackground = Color(0xFFFFFFFF),
			muted = Color(0xFFF0F1F4),
			textSecondary = Color(0xFF676E7E),
			primary = Color(0xFF0EA875),
			onPrimary = Color(0xFFFFFFFF),
			accent = Color(0xFF1EAD7D),
			onAccent = Color(0xFFFFFFFF),
			secondary = Color(0xFFE7E9ED),
			onSecondary = Color(0xFF353C49),
			error = Color(0xFFDC2828),
			border = Color(0xFFDCDEE4),
			inputBorder = Color(0xFFE1E4E9),
			focusRing = Color(0xFF0EA875),
			felt = Color(0xFF428968),
			feltLight = Color(0xFF559F7C),
			gold = Color(0xFFDDA808),
			goldMuted = Color(0xFFA4821D),
			suitRed = Color(0xFFE51919),
			suitBlack = Color(0xFF414857),
		)
	}

	CompositionLocalProvider(
		LocalDarkTheme provides darkTheme,
		LocalTypography provides HandyTypography.with(fontFamily),
		LocalHandyColorScheme provides handyColorScheme,
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

	val colorScheme: HandyColorScheme
		@Composable
		get() = LocalHandyColorScheme.current
}
