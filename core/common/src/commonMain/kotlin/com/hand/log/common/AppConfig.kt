package com.hand.log.common

object AppConfig {
	var isProBuild: Boolean = false
		private set

	fun initialize(isProBuild: Boolean) {
		this.isProBuild = isProBuild
	}
}
