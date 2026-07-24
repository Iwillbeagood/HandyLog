package com.hand.log.data.datasoure.remote

import com.hand.log.common.openAiApiKey
import com.hand.log.data.datasoure.remote.model.OpenAiChatRequest
import com.hand.log.data.datasoure.remote.model.OpenAiChatResponse
import com.hand.log.data.datasoure.remote.model.OpenAiMessage
import com.hand.log.domain.model.preflop.QuizReviewSpot
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

/**
 * OpenAI Chat Completions API(`api.openai.com`)를 직접 호출해 퀴즈 해설을 생성한다.
 * openai-kotlin 의존성 없이 앱의 기존 Ktor/HttpClient 패턴을 재사용한다.
 */
class QuizReviewRemoteDataSourceImpl(
	private val httpClient: HttpClient,
) : QuizReviewRemoteDataSource {

	override suspend fun review(spot: QuizReviewSpot): String {
		require(openAiApiKey.isNotBlank()) { "OpenAI API 키가 설정되지 않았습니다." }

		val request = OpenAiChatRequest(
			model = MODEL,
			messages = listOf(
				OpenAiMessage(role = ROLE_SYSTEM, content = HoldemPromptManager.SYSTEM_PROMPT),
				OpenAiMessage(role = ROLE_USER, content = HoldemPromptManager.buildUserPrompt(spot)),
			),
			temperature = TEMPERATURE,
		)

		val response: OpenAiChatResponse = httpClient.post(CHAT_COMPLETIONS_URL) {
			header(HttpHeaders.Authorization, "Bearer $openAiApiKey")
			contentType(ContentType.Application.Json)
			setBody(request)
		}.body()

		return response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
	}

	private companion object {
		const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
		const val MODEL = "gpt-4o-mini"
		const val TEMPERATURE = 0.3
		const val ROLE_SYSTEM = "system"
		const val ROLE_USER = "user"
	}
}
