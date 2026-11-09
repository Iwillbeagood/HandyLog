import FirebaseAnalytics
import ComposeApp

final class AnalyticsBridgeImpl: AnalyticsBridge {
	func logScreenView(screenName: String) {
		Analytics.logEvent(AnalyticsEventScreenView, parameters: [
			AnalyticsParameterScreenName: screenName,
			AnalyticsParameterScreenClass: screenName,
		])
	}
}
