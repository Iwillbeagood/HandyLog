package com.hand.log.utils.etc

object Logger {

	private const val TAG = "HANDYLOG"
	private const val MAX_LOG_LENGTH = 3000

	fun e(msg: String) = logChunked(msg) { AppLogger.e(TAG, it) }
	fun e(msg: String, throwable: Throwable) = logChunked(msg) { AppLogger.e(TAG, it, throwable) }
	fun w(msg: String) = logChunked("[WARN] $msg") { AppLogger.d(TAG, it) }
	fun i(msg: String) = logChunked(msg) { AppLogger.i(TAG, it) }
	fun d(msg: String) = logChunked(msg) { AppLogger.d(TAG, it) }
	fun v(msg: String) = logChunked("[VERBOSE] $msg") { AppLogger.d(TAG, it) }

	private inline fun logChunked(msg: String, log: (String) -> Unit) {
		if (msg.length <= MAX_LOG_LENGTH) {
			log(msg)
		} else {
			msg.chunked(MAX_LOG_LENGTH).forEachIndexed { index, chunk ->
				log("[$index] $chunk")
			}
		}
	}
}
