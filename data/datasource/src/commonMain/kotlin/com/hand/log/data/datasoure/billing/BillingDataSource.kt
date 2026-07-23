package com.hand.log.data.datasoure.billing

import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult

/**
 * 플랫폼 스토어 결제 SDK 추상화. Android=Play Billing, iOS=StoreKit 2.
 * 서버 검증 없이 스토어의 온디바이스 판정(구매 소유/서명)을 그대로 반환한다.
 */
interface BillingDataSource {

	/** 결제 화면에 표시할 상품 정보. 스토어 미연결·상품 미등록 시 null. */
	suspend fun queryProduct(): ProProduct?

	/** 현재 Pro 상품을 보유 중인지 스토어에 조회한다(앱 시작 시 엔타이틀먼트 동기화용). */
	suspend fun isPurchased(): Boolean

	/** 결제 플로우를 시작하고 결과를 반환한다. 성공 시 소비 불가(비소모성) 상품으로 확정한다. */
	suspend fun purchase(): PurchaseResult

	/** 기존 구매 이력을 복원한다. */
	suspend fun restore(): PurchaseResult
}
