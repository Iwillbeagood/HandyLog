package com.base.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import handylog.core.designsystem.generated.resources.Res
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

val notosanskr
	@Composable
	get() = FontFamily(
		Font(Res.font.pretendard_bold, FontWeight.Bold),
		Font(Res.font.pretendard_medium, FontWeight.Medium),
		Font(Res.font.pretendard_reqular, FontWeight.Normal),
	)

private val notosanskrStyle = TextStyle(
	fontWeight = FontWeight.Normal,
	lineHeightStyle = LineHeightStyle(
		alignment = LineHeightStyle.Alignment.Center,
		trim = LineHeightStyle.Trim.None,
	),
)

val typography = HandyTypography(
	bold32 = notosanskrStyle.copy(
		fontSize = 32.sp,
		lineHeight = 40.sp,
		fontWeight = FontWeight.Bold,
	),
	bold30 = notosanskrStyle.copy(
		fontSize = 30.sp,
		lineHeight = 38.sp,
		fontWeight = FontWeight.Bold,
	),
	bold28 = notosanskrStyle.copy(
		fontSize = 28.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.Bold,
	),
	bold26 = notosanskrStyle.copy(
		fontSize = 26.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.Bold,
	),
	bold24 = notosanskrStyle.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
		fontWeight = FontWeight.Bold,
	),
	medium24 = notosanskrStyle.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
		fontWeight = FontWeight.Medium,
	),
	regular24 = notosanskrStyle.copy(
		fontSize = 24.sp,
		lineHeight = 32.sp,
	),
	bold22 = notosanskrStyle.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
		fontWeight = FontWeight.Bold,
	),
	medium22 = notosanskrStyle.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
		fontWeight = FontWeight.Medium,
	),
	regular22 = notosanskrStyle.copy(
		fontSize = 22.sp,
		lineHeight = 30.sp,
	),
	bold20 = notosanskrStyle.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
		fontWeight = FontWeight.Bold,
	),
	medium20 = notosanskrStyle.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
		fontWeight = FontWeight.Medium,
	),
	regular20 = notosanskrStyle.copy(
		fontSize = 20.sp,
		lineHeight = 28.sp,
	),
	bold18 = notosanskrStyle.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
		fontWeight = FontWeight.Bold,
	),
	medium18 = notosanskrStyle.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
		fontWeight = FontWeight.Medium,
	),
	regular18 = notosanskrStyle.copy(
		fontSize = 18.sp,
		lineHeight = 26.sp,
	),
	bold16 = notosanskrStyle.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Bold,
	),
	medium16 = notosanskrStyle.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Medium,
	),
	regular16 = notosanskrStyle.copy(
		fontSize = 16.sp,
		lineHeight = 24.sp,
	),
	bold14 = notosanskrStyle.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Bold,
		letterSpacing = 0.25.sp,
	),
	medium14 = notosanskrStyle.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = 0.25.sp,
	),
	regular14 = notosanskrStyle.copy(
		fontSize = 14.sp,
		lineHeight = 20.sp,
	),
	bold12 = notosanskrStyle.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Bold,
	),
	medium12 = notosanskrStyle.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Medium,
	),
	regular12 = notosanskrStyle.copy(
		fontSize = 12.sp,
		lineHeight = 16.sp,
	),
	bold10 = notosanskrStyle.copy(
		fontSize = 10.sp,
		fontWeight = FontWeight.Bold,
		lineHeight = 14.sp,
	),
	medium10 = notosanskrStyle.copy(
		fontSize = 10.sp,
		fontWeight = FontWeight.Medium,
		lineHeight = 14.sp,
	),
	regular10 = notosanskrStyle.copy(
		fontSize = 10.sp,
		lineHeight = 14.sp,
	),
	medium8 = notosanskrStyle.copy(
		fontSize = 11.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = (-0.2).sp,
	),
)

@Immutable
data class HandyTypography(
	val bold32: TextStyle,
	val bold30: TextStyle,
	val bold28: TextStyle,

	val bold26: TextStyle,

	val bold24: TextStyle,
	val medium24: TextStyle,
	val regular24: TextStyle,

	val bold22: TextStyle,
	val medium22: TextStyle,
	val regular22: TextStyle,

	val bold20: TextStyle,
	val medium20: TextStyle,
	val regular20: TextStyle,

	val bold18: TextStyle,
	val medium18: TextStyle,
	val regular18: TextStyle,

	val bold16: TextStyle,
	val medium16: TextStyle,
	val regular16: TextStyle,

	val bold14: TextStyle,
	val medium14: TextStyle,
	val regular14: TextStyle,

	val bold12: TextStyle,
	val medium12: TextStyle,
	val regular12: TextStyle,

	val bold10: TextStyle,
	val medium10: TextStyle,
	val regular10: TextStyle,

	val medium8: TextStyle,
)

val LocalTypography = staticCompositionLocalOf {
	HandyTypography(
		bold32 = notosanskrStyle,
		bold30 = notosanskrStyle,
		bold28 = notosanskrStyle,

		bold26 = notosanskrStyle,

		bold24 = notosanskrStyle,
		medium24 = notosanskrStyle,
		regular24 = notosanskrStyle,

		black22 = notosanskrStyle,
		bold22 = notosanskrStyle,
		medium22 = notosanskrStyle,
		regular22 = notosanskrStyle,
		light22 = notosanskrStyle,

		bold20 = notosanskrStyle,
		medium20 = notosanskrStyle,
		regular20 = notosanskrStyle,

		bold18 = notosanskrStyle,
		medium18 = notosanskrStyle,
		regular18 = notosanskrStyle,

		bold16 = notosanskrStyle,
		medium16 = notosanskrStyle,
		regular16 = notosanskrStyle,

		bold14 = notosanskrStyle,
		medium14 = notosanskrStyle,
		regular14 = notosanskrStyle,

		bold12 = notosanskrStyle,
		medium12 = notosanskrStyle,
		regular12 = notosanskrStyle,

		bold10 = notosanskrStyle,
		medium10 = notosanskrStyle,
		regular10 = notosanskrStyle,

		medium8 = notosanskrStyle,
	)
}
