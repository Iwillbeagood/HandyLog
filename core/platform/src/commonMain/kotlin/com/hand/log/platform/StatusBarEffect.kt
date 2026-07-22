package com.hand.log.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun StatusBarEffect(isDarkTheme: Boolean, backgroundColor: Color)
