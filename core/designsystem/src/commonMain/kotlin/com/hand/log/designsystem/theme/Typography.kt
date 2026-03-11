package com.hand.log.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import handylog.core.designsystem.generated.resources.Res
import handylog.core.designsystem.generated.resources.pretendard_bold
import handylog.core.designsystem.generated.resources.pretendard_medium
import handylog.core.designsystem.generated.resources.pretendard_reqular
import org.jetbrains.compose.resources.Font

val TextUnit.nonScaledSp
	@Composable
	@ReadOnlyComposable
	get() = (this.value / LocalDensity.current.fontScale).sp

val TextStyle.nonScaledSp
	@Composable
	@ReadOnlyComposable
	get() = this.copy(
		fontSize = this.fontSize.nonScaledSp,
		lineHeight = this.lineHeight.nonScaledSp,
	)

val pretendard
	@Composable
	get() = FontFamily(
		Font(Res.font.pretendard_bold, FontWeight.Bold),
		Font(Res.font.pretendard_medium, FontWeight.Medium),
		Font(Res.font.pretendard_reqular, FontWeight.Normal),
	)

@Immutable
data class HandyTypography(
	val default: TextStyle,

	val bold32: TextStyle = default.copy(
		fontSize = 32.sp,
		lineHeight = 40.sp,
		fontWeight = FontWeight.Bold,
	),
	val bold30: TextStyle = default.copy(
		fontSize = 30.sp,
		lineHeight = 38.sp,
		fontWeight = FontWeight.Bold,
	),
	val bold28: TextStyle = default.copy(
		fontSize = 28.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.Bold,
	),
	val bold26: TextStyle = default.copy(
		fontSize = 26.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.Bold,
	),
	val bold24: TextStyle = default.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium24: TextStyle = default.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular24: TextStyle = default.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
	),
	val bold22: TextStyle = default.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium22: TextStyle = default.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular22: TextStyle = default.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
	),
	val bold20: TextStyle = default.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium20: TextStyle = default.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular20: TextStyle = default.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
	),
	val bold18: TextStyle = default.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium18: TextStyle = default.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular18: TextStyle = default.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
	),
	val bold16: TextStyle = default.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium16: TextStyle = default.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular16: TextStyle = default.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
	),
	val bold14: TextStyle = default.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Bold,
		letterSpacing = 0.25.sp,
	),
	val medium14: TextStyle = default.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = 0.25.sp,
	),
	val regular14: TextStyle = default.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
	),
	val bold12: TextStyle = default.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium12: TextStyle = default.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular12: TextStyle = default.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
	),
	val bold10: TextStyle = default.copy(
		fontSize = 10.sp,
		lineHeight = 14.sp,
		fontWeight = FontWeight.Bold,
	),
	val medium10: TextStyle = default.copy(
		fontSize = 10.sp,
		lineHeight = 14.sp,
		fontWeight = FontWeight.Medium,
	),
	val regular10: TextStyle = default.copy(
		fontSize = 10.sp,
		lineHeight = 14.sp,
	),
	val medium8: TextStyle = default.copy(
		fontSize = 11.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = (-0.2).sp,
	),
) {
	companion object {
		fun with(
			fontFamily: FontFamily = FontFamily.Default,
			fontWeight: FontWeight = FontWeight.Normal,
		) = HandyTypography(
			default = TextStyle(
				fontFamily = fontFamily,
				fontWeight = fontWeight,
			),
		)
	}
}

internal val LocalTypography = staticCompositionLocalOf<HandyTypography> {
	error("HandyTypography를 provide 해야합니다.")
}
