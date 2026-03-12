package com.hand.log.utils.etc

object Logger {

	private const val TAG = "HANDYLOG"

	fun e(msg: String) {
		AppLogger.e(TAG, msg)
	}

	fun w(msg: String) {
		AppLogger.d(TAG, "[WARN] $msg")
	}

	fun i(msg: String) {
		AppLogger.i(TAG, msg)
	}

	fun d(msg: String) {
		AppLogger.d(TAG, msg)
	}

	fun v(msg: String) {
		AppLogger.d(TAG, "[VERBOSE] $msg")
	}
}
