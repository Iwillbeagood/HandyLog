package com.hand.log

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hand.log.local.datastore.di.initPreferencesDataStore
import com.hand.log.platform.activityProvider

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		initPreferencesDataStore(applicationContext)

		activityProvider = { this }

		enableEdgeToEdge(
			navigationBarStyle = SystemBarStyle.auto(
				lightScrim = Color.TRANSPARENT,
				darkScrim = Color.TRANSPARENT,
			),
		)

		setContent {
			App()
		}
	}
}
