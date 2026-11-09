package com.hand.log.record.quick

import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.quick.QuickHandConfig
import com.hand.log.record.quick.contract.QuickSetup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickSetupTest {

	@Test
	fun `아무것도 입력하지 않으면 BB 단위 블라인드와 스택 미지정으로 유효하다`() {
		val setup = QuickSetup()

		assertEquals(QuickHandConfig.BB_UNIT_BLINDS, setup.blinds)
		assertNull(setup.stackInBb)
		assertTrue(setup.isValid)
	}

	@Test
	fun `SB를 비우면 BB의 절반으로 채운다`() {
		assertEquals(Blinds(sb = 1000.0, bb = 2000.0), QuickSetup(bbText = "2000").blinds)
	}

	@Test
	fun `BB 없이 SB만 있거나 SB가 BB보다 크면 무효다`() {
		assertNull(QuickSetup(sbText = "1000").blinds)
		assertNull(QuickSetup(sbText = "3000", bbText = "2000").blinds)
		assertFalse(QuickSetup(sbText = "1000").isValid)
	}

	@Test
	fun `스택이 숫자가 아니거나 0 이하면 무효다`() {
		assertFalse(QuickSetup(stackText = "abc").isValid)
		assertFalse(QuickSetup(stackText = "0").isValid)
		assertEquals(150.0, QuickSetup(stackText = "150").stackInBb)
	}
}
