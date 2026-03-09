package com.base.app.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalHmmColorScheme = compositionLocalOf<MainColorScheme> {
	error("No KTCColorScheme provided")
}

data class MainColorScheme(
	val white: Color = MainColor.White1,
	val black: Color = MainColor.Black,
	val green2: Color = MainColor.Green1,
	val green4: Color = MainColor.Green2,
	val green5: Color = MainColor.Green3,
	val blue1: Color = MainColor.Blue1,
	val blue2: Color = MainColor.Blue2,
	val blue3: Color = MainColor.Blue3,
	val blue5: Color = MainColor.Blue5,
	val blue6: Color = MainColor.Blue6,
	val red1: Color = MainColor.Red1,
	val red3: Color = MainColor.Red2,
	val gray4: Color = MainColor.Gray4,
	val gray5: Color = MainColor.Gray5,
	val olive1: Color = MainColor.Olive1,
	val purple1: Color = MainColor.Purple1,
	val orange1: Color = MainColor.Orange1,
	val yellow1: Color = MainColor.Yellow1,
	val pink1: Color = MainColor.Pink1,
)

internal object MainColor {
	val Main = Color(0xFF1693ff)

	val White1 = Color(0xFCFFFFFF)
	val White2 = Color(0xFCEEEEEE)

	val Black = Color(0xFF000000)

	val Gray1 = Color(0xFF1F1F1F)
	val Gray2 = Color(0xFF2D2D2D)
	val Gray3 = Color(0xFF414550)
	val Gray4 = Color(0xFF545454)
	val Gray5 = Color(0xFF72757D)
	val Gray6 = Color(0xFF868686)
	val Gray7 = Color(0xFFB0B0B0)
	val Gray8 = Color(0xFFC2C2C2)
	val Gray9 = Color(0xFFDCDCDC)
	val Gray10 = Color(0xFFF2F3F6)
	val Gray11 = Color(0xFFFDFDFD)

	val Yellow1 = Color(0xFFffeb00)

	val Blue1 = Color(0xFF016acb)
	val Blue2 = Color(0xFF447BFD)
	val Blue3 = Color(0xFF7ac1ff)
	val Blue4 = Color(0xFFdee0e4)
	val Blue5 = Color(0xFFEDF2FF)
	val Blue6 = Color(0xFF3BA4FF)

	val Orange1 = Color(0xFFf6a819)

	val Red1 = Color(0xFF9E0000)
	val Red2 = Color(0xFFEA5D5D)
	val Red3 = Color(0xFFFD4444)

	val Pink1 = Color(0xFFF8DCDD)

	val Green1 = Color(0xFF43902d)
	val Green2 = Color(0xFF47c73c)
	val Green3 = Color(0xFF2ECC71)

	val Olive1 = Color(0xFF999900)

	val Purple1 = Color(0xFFa75bf3)

}
