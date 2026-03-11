package com.hand.log.utils.etc

import android.util.Log
import com.hand.log.utils.BuildConfig.*
import kotlin.collections.get

object Logger : AndroidLogger {

	private const val TAG = "BAEKTONG"
	private const val MAX_LOG_LENGTH = 1000

	override fun e(msg: String) {
		if (DEBUG) logLongMessage(Log.ERROR, buildLogMsg(msg))
	}

	override fun w(msg: String) {
		if (DEBUG) logLongMessage(Log.WARN, buildLogMsg(msg))
	}

	override fun i(msg: String) {
		if (DEBUG) logLongMessage(Log.INFO, buildLogMsg(msg))
	}

	override fun d(msg: String) {
		if (DEBUG) logLongMessage(Log.DEBUG, buildLogMsg(msg))
	}

	override fun v(msg: String) {
		if (DEBUG) logLongMessage(Log.VERBOSE, buildLogMsg(msg))
	}

	private fun logLongMessage(logType: Int, message: String) {
		var i = 0
		while (i < message.length) {
			val end = Math.min(message.length, i + MAX_LOG_LENGTH)
			val part = message.substring(i, end)
			when (logType) {
				Log.ERROR -> Log.e(TAG, part)
				Log.WARN -> Log.w(TAG, part)
				Log.INFO -> Log.i(TAG, part)
				Log.DEBUG -> Log.d(TAG, part)
				Log.VERBOSE -> Log.v(TAG, part)
			}
			i += MAX_LOG_LENGTH
		}
	}

	private fun buildLogMsg(msg: String): String {
		val ste = Thread.currentThread().stackTrace[4]
		val sb = StringBuilder().apply {
			append("[")
			append(ste.fileName.replace(".kotlin", ""))
			append("::")
			append(ste.methodName)
			append("] ")
			append(msg)
		}
		return sb.toString()
	}
}
