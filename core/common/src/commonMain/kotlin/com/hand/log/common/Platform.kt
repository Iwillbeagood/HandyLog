package com.hand.log.common

expect val platformName: String

val isIos: Boolean get() = platformName == "iOS"

val notionToken: String = BuildKonfig.NOTION_TOKEN

val slackWebhookUrl: String = BuildKonfig.SLACK_WEBHOOK_URL

val openAiApiKey: String = BuildKonfig.OPENAI_API_KEY
