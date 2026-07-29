package com.hand.log.domain.repository

import com.hand.log.domain.model.billing.ProProduct
import com.hand.log.domain.model.billing.PurchaseResult
import kotlinx.coroutines.flow.Flow

/**
 * 서버 없는 Pro 해금 엔타이틀먼트 저장소.
 *
 * Pro 여부는 컴파일 타임 값이 아니라 런타임 상태다. 스토어(Play Billing / StoreKit)의 판정을
 * 로컬(DataStore)에 캐시하여 오프라인에서도 유지하고, [refresh]/[purchase]/[restore] 시 스토어와 재동기화한다.
 */
interface ProEntitlementRepository {

	/** 로컬 캐시 기반의 Pro 활성화 여부. 앱 전역에서 이 Flow 하나로 게이팅한다. */
	fun observeIsPro(): Flow<Boolean>

	/** 스토어에서 현재 보유 상태를 조회해 로컬 캐시를 갱신한다. 앱 시작 시 호출. */
	suspend fun refresh()

	/** 결제 화면에 표시할 상품 가격 정보. 스토어 미연결 시 null. */
	suspend fun getProduct(): ProProduct?

	/** Pro 상품 결제 플로우를 시작한다. */
	suspend fun purchase(): PurchaseResult

	/** 기존 구매 이력을 복원한다(기기 변경·재설치 대응). */
	suspend fun restore(): PurchaseResult

	companion object {
		const val PRO_PRODUCT_ID = "handylog_pro_unlock"
	}
}
