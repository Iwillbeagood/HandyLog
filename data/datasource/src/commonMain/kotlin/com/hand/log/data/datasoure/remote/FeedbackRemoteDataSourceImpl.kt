package com.hand.log.data.datasoure.remote

import com.hand.log.common.AppConfig
import com.hand.log.common.notionToken
import com.hand.log.common.platformName
import com.hand.log.data.datasoure.remote.model.NotionPageRequest
import com.hand.log.data.datasoure.remote.model.NotionParent
import com.hand.log.domain.model.Feedback
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Notion API(`api.notion.com`)를 직접 호출하여 피드백 트래킹 DB 에 페이지를 생성한다.
 *
 * 필요한 Notion DB 속성(이름 일치 필수):
 * - `제목` (Title), `상태` (Select),
 *   `내용` (Rich text), `연락처` (Rich text), `플랫폼` (Select)
 */
class FeedbackRemoteDataSourceImpl(
	private val httpClient: HttpClient,
) : FeedbackRemoteDataSource {

	override suspend fun submit(feedback: Feedback) {
		val token = notionToken
		val databaseId = AppConfig.NOTION_DATABASE_ID
		require(token.isNotBlank() && databaseId.isNotBlank()) {
			"Notion 토큰/DB ID 가 설정되지 않았습니다."
		}

		val request = NotionPageRequest(
			parent = NotionParent(databaseId = databaseId),
			properties = buildJsonObject {
				putTitle(PROP_TITLE, feedback.title)
				putSelect(PROP_STATUS, STATUS_NEW)
				putRichText(PROP_CONTENT, feedback.content)
				if (feedback.email.isNotBlank()) {
					putRichText(PROP_CONTACT, feedback.email)
				}
				putSelect(PROP_PLATFORM, platformName)
			},
		)

		httpClient.post(NOTION_PAGES_URL) {
			header(HttpHeaders.Authorization, "Bearer $token")
			header(NOTION_VERSION_HEADER, NOTION_VERSION)
			contentType(ContentType.Application.Json)
			setBody(request)
		}
	}

	private fun JsonObjectBuilder.putTitle(name: String, value: String) {
		putJsonObject(name) {
			putJsonArray("title") {
				add(textNode(value))
			}
		}
	}

	private fun JsonObjectBuilder.putRichText(name: String, value: String) {
		putJsonObject(name) {
			putJsonArray("rich_text") {
				add(textNode(value))
			}
		}
	}

	private fun JsonObjectBuilder.putSelect(name: String, value: String) {
		putJsonObject(name) {
			putJsonObject("select") {
				put("name", value)
			}
		}
	}

	private fun textNode(value: String) = buildJsonObject {
		putJsonObject("text") {
			put("content", value.take(NOTION_TEXT_LIMIT))
		}
	}

	private companion object {
		const val NOTION_PAGES_URL = "https://api.notion.com/v1/pages"
		const val NOTION_VERSION_HEADER = "Notion-Version"
		const val NOTION_VERSION = "2022-06-28"
		const val NOTION_TEXT_LIMIT = 2000

		const val PROP_TITLE = "제목"
		const val PROP_STATUS = "상태"
		const val PROP_CONTENT = "내용"
		const val PROP_CONTACT = "연락처"
		const val PROP_PLATFORM = "플랫폼"

		const val STATUS_NEW = "신규"
	}
}
