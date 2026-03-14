package com.hand.log.ui.poker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

object SheetDragBlocker : NestedScrollConnection {
	override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available

	override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}
