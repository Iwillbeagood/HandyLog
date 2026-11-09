package com.hand.log.platform.etc

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

actual object AppAnalytics {

	actual fun logScreenView(screenName: String) {
		Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
			param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
			param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
		}
	}
}
