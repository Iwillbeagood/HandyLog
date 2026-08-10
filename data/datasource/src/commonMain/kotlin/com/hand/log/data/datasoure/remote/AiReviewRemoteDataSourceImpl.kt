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

class AiReviewRemoteDataSourceImpl(
	private val httpClient: HttpClient,
) : AiReviewRemoteDataSource {

	override suspend fun reviewQuizSpot(spot: QuizReviewSpot): String = chat(
		systemPrompt = HoldemPromptManager.SYSTEM_PROMPT,
		userPrompt = HoldemPromptManager.buildUserPrompt(spot),
		temperature = QUIZ_TEMPERATURE,
	)

	override suspend fun reviewHand(handHistory: String, languageName: String): String = chat(
		systemPrompt = HandReviewPromptManager.SYSTEM_PROMPT,
		userPrompt = HandReviewPromptManager.buildUserPrompt(handHistory, languageName),
		temperature = HAND_TEMPERATURE,
	)

	private suspend fun chat(systemPrompt: String, userPrompt: String, temperature: Double): String {
		require(openAiApiKey.isNotBlank()) { "OpenAI API 키가 설정되지 않았습니다." }

		val request = OpenAiChatRequest(
			model = MODEL,
			messages = listOf(
				OpenAiMessage(role = ROLE_SYSTEM, content = systemPrompt),
				OpenAiMessage(role = ROLE_USER, content = userPrompt),
			),
			temperature = temperature,
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
		const val QUIZ_TEMPERATURE = 0.3
		const val HAND_TEMPERATURE = 0.5
		const val ROLE_SYSTEM = "system"
		const val ROLE_USER = "user"
	}
}
