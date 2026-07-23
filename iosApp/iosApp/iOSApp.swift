import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
	init() {
		// StoreKit 2 인앱 결제 브리지를 Kotlin(iosMain) 쪽에 주입한다.
		if #available(iOS 15.0, *) {
			StoreKitBridgeRegistry.shared.bridge = StoreKitBridgeImpl()
		}
	}

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}