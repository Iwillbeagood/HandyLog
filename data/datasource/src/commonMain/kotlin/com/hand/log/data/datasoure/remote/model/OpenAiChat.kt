package com.hand.log.data.datasoure.remote.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OpenAiChatRequest(
	val model: String,
	val messages: List<OpenAiMessage>,
	val temperature: Double,
	@EncodeDefault(EncodeDefault.Mode.NEVER)
	@SerialName("max_tokens")
	val maxTokens: Int? = null,
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
