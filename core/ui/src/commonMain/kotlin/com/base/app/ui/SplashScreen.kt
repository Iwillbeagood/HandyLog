package com.hand.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SplashScreen() {
	var visible by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.navigationBarsPadding()
			.background(MaterialTheme.colorScheme.primary),
	) {

	}

	LaunchedEffect(Unit) {
		visible = true
	}
}