package com.hand.log.domain.usecase

import com.hand.log.domain.model.Feedback
import com.hand.log.domain.repository.FeedbackRepository

/**
 * 문의/피드백 제출. 입력값을 trim·검증한 뒤 Repository 로 전달한다.
 * - 제목/내용은 필수.
 * - 결과는 [Result] 로 반환하여 ViewModel 이 성공/실패를 구분할 수 있게 한다.
 */
class SubmitFeedbackUseCase(
	private val feedbackRepository: FeedbackRepository,
) {

	suspend operator fun invoke(feedback: Feedback): Result<Unit> {
		val trimmed = feedback.copy(
			title = feedback.title.trim(),
			content = feedback.content.trim(),
			email = feedback.email.trim(),
		)
		if (trimmed.title.isBlank() || trimmed.content.isBlank()) {
			return Result.failure(IllegalArgumentException("title and content must not be blank"))
		}
		return runCatching { feedbackRepository.submit(trimmed) }
	}
}
