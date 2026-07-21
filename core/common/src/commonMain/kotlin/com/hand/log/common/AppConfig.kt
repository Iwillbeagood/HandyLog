package com.hand.log.common

object AppConfig {
	var isProBuild: Boolean = false
		private set

	const val NOTION_DATABASE_ID = "3a48c719c24d8110807bc0688e0c13d2"

	fun initialize(isProBuild: Boolean) {
		this.isProBuild = isProBuild
	}
}
