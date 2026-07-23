package com.hand.log.domain.model.billing

/**
 * 인앱 결제/복원 시도 결과. 서버 없이 스토어(Play Billing / StoreKit)의 판정을 그대로 전달한다.
 */
sealed interface PurchaseResult {
	/** 구매(또는 복원)로 Pro 권한이 활성화됨. */
	data object Success : PurchaseResult

	/** 이미 보유한 상품이라 별도 결제 없이 Pro 권한이 확인됨. */
	data object AlreadyOwned : PurchaseResult

	/** 사용자가 결제 창을 닫아 취소함. */
	data object Cancelled : PurchaseResult

	/** 결제가 보류 상태(예: 승인 대기). 완료 시점에 다시 확인해야 함. */
	data object Pending : PurchaseResult

	/** 복원할 구매 이력이 없음. */
	data object NothingToRestore : PurchaseResult

	/** 스토어 연결 실패 등 오류. */
	data class Failure(val message: String?) : PurchaseResult
}
