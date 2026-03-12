package com.hand.log.designsystem.etc

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal interface MultipleEventsCutter {
	fun processEvent(event: () -> Unit)

	companion object
}

internal fun MultipleEventsCutter.Companion.get(): MultipleEventsCutter =
	MultipleEventsCutterImpl()

private class MultipleEventsCutterImpl : MultipleEventsCutter {
	@OptIn(ExperimentalTime::class)
	private val now: Long
		get() = Clock.System.now().toEpochMilliseconds()

	private var lastEventTimeMs: Long = 0

	override fun processEvent(event: () -> Unit) {
		if (now - lastEventTimeMs >= 300L) {
			event.invoke()
		}
		lastEventTimeMs = now
	}
}
