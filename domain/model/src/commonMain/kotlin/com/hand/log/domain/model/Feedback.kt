package com.hand.log.domain.model

/**
 * 앱 내 "문의하기"(기능 건의 및 오류 신고) 입력 데이터.
 */
data class Feedback(
	val category: FeedbackCategory,
	val title: String,
	val content: String,
	val email: String,
)

enum class FeedbackCategory {
	/** 건의사항 */
	FEATURE,

	/** 오류 신고 */
	ERROR,
}
