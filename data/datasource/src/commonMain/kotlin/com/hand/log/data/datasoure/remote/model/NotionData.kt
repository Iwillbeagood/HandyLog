package com.hand.log.data.datasoure.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * Notion `POST /v1/pages` 요청 본문.
 * `properties` 는 속성 타입마다, `children` 은 블록 타입마다 형태가 달라 직접 구성한다.
 */
@Serializable
data class NotionPageRequest(
	val parent: NotionParent,
	val properties: JsonObject,
	val children: JsonArray? = null,
)

@Serializable
data class NotionParent(
	@SerialName("database_id")
	val databaseId: String,
)
