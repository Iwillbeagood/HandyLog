package com.hand.log.domain.model

/**
 * 앱 내 "문의하기"(기능 건의 및 오류 신고) 입력 데이터.
 */
data class Feedback(
	val title: String,
	val content: String,
	val email: String,
	val images: List<FeedbackImage> = emptyList(),
)

/**
 * 피드백에 첨부하는 이미지의 원본 바이트와 MIME 타입.
 */
class FeedbackImage(
	val bytes: ByteArray,
	val mimeType: String,
)
