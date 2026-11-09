import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAnalytics

@main
struct iOSApp: App {
	init() {
		// StoreKit 2 인앱 결제 브리지를 Kotlin(iosMain) 쪽에 주입한다.
		if #available(iOS 15.0, *) {
			StoreKitBridgeRegistry.shared.bridge = StoreKitBridgeImpl()
		}
		if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
			FirebaseApp.configure()
			#if DEBUG
			Analytics.setAnalyticsCollectionEnabled(false)
			#endif
			AnalyticsBridgeRegistry.shared.bridge = AnalyticsBridgeImpl()
		}
	}

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}