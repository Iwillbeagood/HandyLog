package com.hand.log.settings.main.contract

import com.hand.log.domain.model.ThemeMode

data class AppSettings(
	val themeMode: ThemeMode = ThemeMode.AUTO,
	val betSizePresets: List<Double> = listOf(2.0, 2.5, 3.0, 4.0, 5.0),
)
