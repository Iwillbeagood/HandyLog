import Foundation
import StoreKit
import ComposeApp

/// StoreKit 2 기반 서버리스 인앱 결제 구현.
/// Kotlin(iosMain)의 `StoreKitBridge` 를 구현하여 `StoreKitBridgeRegistry` 에 주입한다.
/// 서명 검증은 StoreKit 이 온디바이스(JWS)로 수행하므로 서버가 필요 없다.
@available(iOS 15.0, *)
final class StoreKitBridgeImpl: StoreKitBridge {

	/// domain 의 ProEntitlementRepository.PRO_PRODUCT_ID 와 반드시 일치해야 한다.
	private let productId = "handylog_pro_unlock"

	func queryProduct(onResult: @escaping (String?, String?) -> Void) {
		Task {
			do {
				let products = try await Product.products(for: [productId])
				if let product = products.first {
					onResult(product.id, product.displayPrice)
				} else {
					onResult(nil, nil)
				}
			} catch {
				onResult(nil, nil)
			}
		}
	}

	func isPurchased(onResult: @escaping (KotlinBoolean) -> Void) {
		Task {
			let owned = await hasEntitlement()
			onResult(KotlinBoolean(bool: owned))
		}
	}

	func purchase(onResult: @escaping (String, String?) -> Void) {
		Task {
			do {
				let products = try await Product.products(for: [productId])
				guard let product = products.first else {
					onResult("failure", "Product unavailable")
					return
				}
				let result = try await product.purchase()
				switch result {
				case .success(let verification):
					switch verification {
					case .verified(let transaction):
						await transaction.finish()
						onResult("success", nil)
					case .unverified:
						onResult("failure", "Unverified transaction")
					}
				case .userCancelled:
					onResult("cancelled", nil)
				case .pending:
					onResult("pending", nil)
				@unknown default:
					onResult("failure", nil)
				}
			} catch {
				onResult("failure", error.localizedDescription)
			}
		}
	}

	func restore(onResult: @escaping (String, String?) -> Void) {
		Task {
			try? await AppStore.sync()
			let owned = await hasEntitlement()
			onResult(owned ? "success" : "nothing", nil)
		}
	}

	private func hasEntitlement() async -> Bool {
		for await result in Transaction.currentEntitlements {
			if case .verified(let transaction) = result,
				transaction.productID == productId,
				transaction.revocationDate == nil {
				return true
			}
		}
		return false
	}
}
