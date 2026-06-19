package com.hand.log.common

object AppConfig {
	var isProBuild: Boolean = false
		private set

	const val NOTION_DATABASE_ID = "d8738e2ea888417b953be52a7b06a602"

	fun initialize(isProBuild: Boolean) {
		this.isProBuild = isProBuild
	}
}
