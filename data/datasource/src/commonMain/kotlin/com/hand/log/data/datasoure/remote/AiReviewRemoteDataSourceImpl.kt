package com.hand.log.data.datasoure.remote

import com.hand.log.common.openAiApiKey
import com.hand.log.data.datasoure.remote.model.OpenAiChatRequest
import com.hand.log.data.datasoure.remote.model.OpenAiChatResponse
import com.hand.log.data.datasoure.remote.model.OpenAiMessage
import com.hand.log.domain.model.preflop.QuizReviewSpot
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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

	override suspend fun isAvailable(): Boolean {
		if (openAiApiKey.isBlank()) return false
		val error = runCatching { chat(PROBE_PROMPT, PROBE_PROMPT, PROBE_TEMPERATURE, maxTokens = 1) }
			.exceptionOrNull() ?: return true
		return error !is AiReviewUnavailableException
	}

	private suspend fun chat(
		systemPrompt: String,
		userPrompt: String,
		temperature: Double,
		maxTokens: Int? = null,
	): String {
		require(openAiApiKey.isNotBlank()) { "OpenAI API 키가 설정되지 않았습니다." }

		val request = OpenAiChatRequest(
			model = MODEL,
			messages = listOf(
				OpenAiMessage(role = ROLE_SYSTEM, content = systemPrompt),
				OpenAiMessage(role = ROLE_USER, content = userPrompt),
			),
			temperature = temperature,
			maxTokens = maxTokens,
		)

		val response: OpenAiChatResponse = try {
			httpClient.post(CHAT_COMPLETIONS_URL) {
				header(HttpHeaders.Authorization, "Bearer $openAiApiKey")
				contentType(ContentType.Application.Json)
				setBody(request)
			}.body()
		} catch (e: ClientRequestException) {
			if (isUnavailable(e)) throw AiReviewUnavailableException(e)
			throw e
		}

		return response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
	}

	private suspend fun isUnavailable(error: ClientRequestException): Boolean {
		val status = error.response.status
		if (status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden) return true
		val body = runCatching { error.response.bodyAsText() }.getOrNull().orEmpty()
		return body.contains(QUOTA_MARKER) || body.contains(CREDIT_MARKER)
	}

	private companion object {
		const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
		const val MODEL = "gpt-4o-mini"
		const val QUIZ_TEMPERATURE = 0.3
		const val HAND_TEMPERATURE = 0.5
		const val PROBE_TEMPERATURE = 0.0
		const val ROLE_SYSTEM = "system"
		const val ROLE_USER = "user"
		const val PROBE_PROMPT = "ping"
		const val QUOTA_MARKER = "insufficient_quota"
		const val CREDIT_MARKER = "credit_balance_exhausted"
	}
}
