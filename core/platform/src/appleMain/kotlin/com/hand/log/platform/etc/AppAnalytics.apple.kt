package com.hand.log.platform.etc

interface AnalyticsBridge {
	fun logScreenView(screenName: String)
}

object AnalyticsBridgeRegistry {
	var bridge: AnalyticsBridge? = null
}

actual object AppAnalytics {

	actual fun logScreenView(screenName: String) {
		AnalyticsBridgeRegistry.bridge?.logScreenView(screenName)
	}
}
