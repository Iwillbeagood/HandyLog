package com.hand.log.common

object Constants {

	val BASE_URI = if (BuildConfig.DEBUG) {
		"https://test-api3.logishm.com:8443/api/"
	} else {
		"https://api.logishm.com:8443/api/"
	}

	val RETRY_HOST = if (BuildConfig.DEBUG) {
		BASE_URI
	} else {
		"http://222.231.9.204:8080/"
	}

	val HOST_URLS = if (BuildConfig.DEBUG) {
		listOf(BASE_URI, RETRY_HOST)
	} else {
		listOf(BASE_URI, RETRY_HOST)
	}

	val TERMS_BASE_URL = "https://api.logishm.com:8443/api/"

	val NICE_VERIFY_URL = if (BuildConfig.DEBUG) {
		"http://cargo.apitest.labbgsoft.kr:8080/nice_test_3/chk"
	} else {
		"https://auth.bgsoft.kr:8443/nice_module/chk"
	}

	const val DEFAULT_TOKEN = "-"

	const val GOOGLE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=kic.owner2"

	const val CALL_CENTER_NUMBER = "16600303"

	private const val BAEKTONG_DEEPLINK_ADDRESS = "baektong://baektong.com/"

	fun baekTongDeeplink(url: String) = "$BAEKTONG_DEEPLINK_ADDRESS$url"

	fun baekTongDeeplinkWithArgument(url: String, vararg arguments: String): String {
		val argumentPath = arguments.joinToString("/") { "{$it}" }
		return "$BAEKTONG_DEEPLINK_ADDRESS$url/$argumentPath"
	}

	fun cargoCardSharedContentKey(orderNumber: String) = "cargoCard-$orderNumber"
}
