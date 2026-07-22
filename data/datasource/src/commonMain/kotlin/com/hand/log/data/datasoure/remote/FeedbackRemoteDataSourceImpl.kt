package com.hand.log.data.datasoure.remote

import com.hand.log.common.AppConfig
import com.hand.log.common.notionToken
import com.hand.log.common.platformName
import com.hand.log.common.slackWebhookUrl
import com.hand.log.data.datasoure.remote.model.NotionPageRequest
import com.hand.log.data.datasoure.remote.model.NotionParent
import com.hand.log.domain.model.Feedback
import com.hand.log.domain.model.FeedbackImage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Notion API(`api.notion.com`)를 직접 호출하여 피드백 트래킹 DB 에 페이지를 생성한다.
 * 첨부 이미지는 File Upload API 로 업로드한 뒤 페이지 본문에 이미지 블록으로 추가한다.
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

		val uploadIds = feedback.images.mapIndexedNotNull { index, image ->
			uploadImage(token, image, index)
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
			children = uploadIds.toImageBlocks(),
		)

		val page: JsonObject = httpClient.post(NOTION_PAGES_URL) {
			header(HttpHeaders.Authorization, "Bearer $token")
			header(NOTION_VERSION_HEADER, NOTION_VERSION)
			contentType(ContentType.Application.Json)
			setBody(request)
		}.body()

		notifySlack(feedback, page["url"]?.jsonPrimitive?.content)
	}

	/**
	 * 문의 등록 성공 시 Slack Incoming Webhook 으로 알린다.
	 * Slack 만으로 답변까지 처리할 수 있도록 내용 전문과, 클릭 시 답장 메일창이 열리는 mailto 링크를 포함한다.
	 * 웹훅 미설정이거나 전송 실패해도 문의 등록 자체는 성공으로 유지한다.
	 */
	private suspend fun notifySlack(feedback: Feedback, pageUrl: String?) {
		if (slackWebhookUrl.isBlank()) return

		val imageNote = if (feedback.images.isNotEmpty()) " · 이미지 ${feedback.images.size}장" else ""
		val contactLine = if (feedback.email.isNotBlank()) {
			"*연락처* <${buildReplyMailto(feedback)}|✉️ ${feedback.email}> (클릭해 답장)"
		} else {
			"*연락처* 없음 · 답변 불가"
		}
		val text = buildString {
			append("*새 문의가 등록되었습니다*").append(imageNote).append("\n\n")
			append("*제목* ").append(feedback.title).append("\n")
			append(feedback.content).append("\n\n")
			append("*플랫폼* ").append(platformName).append("\n")
			append(contactLine)
			pageUrl?.let { append("\n<").append(it).append("|Notion에서 열기>") }
		}

		runCatching {
			httpClient.post(slackWebhookUrl) {
				contentType(ContentType.Application.Json)
				setBody(buildJsonObject { put("text", text) })
			}
		}
	}

	private fun buildReplyMailto(feedback: Feedback): String {
		val subject = REPLY_SUBJECT.encodeURLParameter()
		val body = """
			안녕하세요, HandyLog입니다.
			문의해 주셔서 감사합니다. 아래 내용을 검토한 후 답변드립니다.

			── 원문 문의 ──────────────────────────
			제목: ${feedback.title}

			${feedback.content.take(REPLY_QUOTE_LIMIT)}
			────────────────────────────────────────

			── 답변 ────────────────────────────────
			(여기에 답변 내용을 작성해 주세요.)
			────────────────────────────────────────

			감사합니다.
			HandyLog 드림
		""".trimIndent().encodeURLParameter()
		return "mailto:${feedback.email}?subject=$subject&body=$body"
	}

	/**
	 * 이미지를 Notion File Upload API 로 업로드하고 file_upload id 를 반환한다.
	 * 1) `POST /v1/file_uploads` 로 업로드 슬롯 생성 → 2) 반환된 URL 에 바이트 전송(multipart).
	 */
	private suspend fun uploadImage(token: String, image: FeedbackImage, index: Int): String? {
		val fileName = "feedback_${index + 1}.${image.mimeType.toFileExtension()}"

		val created: JsonObject = httpClient.post(NOTION_FILE_UPLOADS_URL) {
			header(HttpHeaders.Authorization, "Bearer $token")
			header(NOTION_VERSION_HEADER, NOTION_VERSION)
			contentType(ContentType.Application.Json)
			setBody(
				buildJsonObject {
					put("filename", fileName)
					put("content_type", image.mimeType)
				},
			)
		}.body()

		val uploadId = created["id"]?.jsonPrimitive?.content ?: return null
		val uploadUrl = created["upload_url"]?.jsonPrimitive?.content ?: return null

		httpClient.post(uploadUrl) {
			header(HttpHeaders.Authorization, "Bearer $token")
			header(NOTION_VERSION_HEADER, NOTION_VERSION)
			setBody(
				MultiPartFormDataContent(
					formData {
						append(
							key = "file",
							value = image.bytes,
							headers = Headers.build {
								append(HttpHeaders.ContentType, image.mimeType)
								append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
							},
						)
					},
				),
			)
		}

		return uploadId
	}

	private fun List<String>.toImageBlocks(): JsonArray? {
		if (isEmpty()) return null
		return buildJsonArray {
			forEach { uploadId ->
				add(
					buildJsonObject {
						put("object", "block")
						put("type", "image")
						putJsonObject("image") {
							put("type", "file_upload")
							putJsonObject("file_upload") {
								put("id", uploadId)
							}
						}
					},
				)
			}
		}
	}

	private fun String.toFileExtension(): String = when (this) {
		"image/png" -> "png"
		"image/webp" -> "webp"
		"image/gif" -> "gif"
		"image/heic" -> "heic"
		else -> "jpg"
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
		const val NOTION_FILE_UPLOADS_URL = "https://api.notion.com/v1/file_uploads"
		const val NOTION_VERSION_HEADER = "Notion-Version"
		const val NOTION_VERSION = "2022-06-28"
		const val NOTION_TEXT_LIMIT = 2000

		// Slack mailto 답장 링크의 제목/본문 양식 (사용 시 퍼센트 인코딩)
		const val REPLY_SUBJECT = "[HandyLog] 문의 답변"
		const val REPLY_QUOTE_LIMIT = 500

		const val PROP_TITLE = "제목"
		const val PROP_STATUS = "상태"
		const val PROP_CONTENT = "내용"
		const val PROP_CONTACT = "연락처"
		const val PROP_PLATFORM = "플랫폼"

		const val STATUS_NEW = "신규"
	}
}
