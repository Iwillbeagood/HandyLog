package com.hand.log.data.datasoure.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiChatRequest(
	val model: String,
	val messages: List<OpenAiMessage>,
	val temperature: Double,
)

@Serializable
data class OpenAiMessage(
	val role: String,
	val content: String,
)

@Serializable
data class OpenAiChatResponse(
	val choices: List<OpenAiChoice> = emptyList(),
)

@Serializable
data class OpenAiChoice(
	val message: OpenAiMessage,
)
