package com.hand.log.designsystem.etc

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
	name = "Light Mode",
	showBackground = true,
	uiMode = UI_MODE_NIGHT_NO,
	backgroundColor = 0xFFFFFFFF,
)
@Preview(
	name = "Dark Mode",
	showBackground = true,
	uiMode = UI_MODE_NIGHT_YES,
	backgroundColor = 0xFF000000,
)
annotation class ThemePreviews
