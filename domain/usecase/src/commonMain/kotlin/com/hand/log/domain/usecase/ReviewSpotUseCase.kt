package com.hand.log.domain.usecase

import com.hand.log.domain.model.preflop.QuizReviewSpot
import com.hand.log.domain.repository.QuizReviewRepository

/**
 * 프리플랍 퀴즈 스팟에 대한 LLM 해설을 요청한다.
 * 네트워크/키 미설정 등 실패는 [Result] 로 감싸 ViewModel 이 폴백 UI 를 띄울 수 있게 한다.
 */
class ReviewSpotUseCase(
	private val quizReviewRepository: QuizReviewRepository,
) {

	suspend operator fun invoke(spot: QuizReviewSpot): Result<String> =
		runCatching { quizReviewRepository.review(spot) }
			.mapCatching { it.ifBlank { error("empty review") } }
}
