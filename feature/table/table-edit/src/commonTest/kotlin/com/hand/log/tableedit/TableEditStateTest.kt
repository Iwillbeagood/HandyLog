package com.hand.log.tableedit

import com.hand.log.tableedit.contract.TableEditState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TableEditStateTest {

	@Test
	fun cashTableRequiresPositiveBigBlind() {
		val base = TableEditState(date = "2026-01-01", isCash = true)
		assertFalse(base.copy(sbText = "", bbText = "").isSubmitEnabled)
		assertFalse(base.copy(sbText = "0", bbText = "0").isSubmitEnabled)
		assertTrue(base.copy(sbText = "0.5", bbText = "1").isSubmitEnabled)
	}

	@Test
	fun tournamentIgnoresBlindFields() {
		assertTrue(TableEditState(date = "2026-01-01", isCash = false).isSubmitEnabled)
	}
}
