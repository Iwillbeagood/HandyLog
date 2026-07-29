package com.hand.log.data.datasoure.billing

/**
 * StoreKit 2 는 Swift 전용(async) API라 Kotlin/Native 에서 직접 호출할 수 없다.
 * 따라서 Swift 가 이 인터페이스를 구현하고, 앱 시작 시 [StoreKitBridgeRegistry] 에 주입한다.
 * 콜백 기반으로 정의하여 Swift closure 로 자연스럽게 브리징되도록 한다.
 *
 * 결과 코드 문자열: "success" | "already_owned" | "cancelled" | "pending" | "nothing" | "failure".
 */
interface StoreKitBridge {
	fun queryProduct(onResult: (id: String?, formattedPrice: String?) -> Unit)
	fun isPurchased(onResult: (Boolean) -> Unit)
	fun purchase(onResult: (code: String, message: String?) -> Unit)
	fun restore(onResult: (code: String, message: String?) -> Unit)
}

/** Swift 가 앱 시작 시 구현체를 등록하는 지점. */
object StoreKitBridgeRegistry {
	var bridge: StoreKitBridge? = null
}
