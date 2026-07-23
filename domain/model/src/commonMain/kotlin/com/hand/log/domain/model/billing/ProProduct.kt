package com.hand.log.domain.model.billing

/**
 * 스토어에서 조회한 Pro 해금 상품 정보.
 * [formattedPrice] 는 로케일·통화가 적용된 스토어 표기 가격(예: "₩4,900")이라 앱에서 직접 포맷하지 않는다.
 */
data class ProProduct(
	val id: String,
	val formattedPrice: String,
)
