import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
	func makeUIViewController(context: Context) -> UIViewController {
		let composeVC = MainViewControllerKt.mainViewController()
		return StatusBarHostingController(childVC: composeVC)
	}

	func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
	var body: some View {
		ComposeView()
			.ignoresSafeArea(.container, edges: [.top, .bottom])
			.ignoresSafeArea(.keyboard)
	}
}
