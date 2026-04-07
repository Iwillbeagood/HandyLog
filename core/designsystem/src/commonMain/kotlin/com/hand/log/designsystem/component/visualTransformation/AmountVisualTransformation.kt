package com.hand.log.designsystem.component.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 금액 형식으로 변환하는 VisualTransformation
 * 형식: 1,000,000 (천 단위 콤마)
 */
fun amountVisualTransformation(): VisualTransformation = VisualTransformation { text ->
	val raw = text.text.filter { it.isDigit() }

	if (raw.isEmpty()) {
		return@VisualTransformation TransformedText(text, OffsetMapping.Identity)
	}

	val formatted = buildString {
		raw.reversed().forEachIndexed { index, char ->
			if (index > 0 && index % 3 == 0) {
				append(',')
			}
			append(char)
		}
	}.reversed()

	val offsetMapping = object : OffsetMapping {
		override fun originalToTransformed(offset: Int): Int {
			if (offset == 0) return 0

			val commasBeforeOffset = (raw.length - offset) / 3
			val totalCommas = (raw.length - 1) / 3

			return offset + (totalCommas - commasBeforeOffset)
		}

		override fun transformedToOriginal(offset: Int): Int {
			if (offset == 0) return 0

			var currentPos = 0

			raw.reversed().forEachIndexed { index, _ ->
				if (index > 0 && index % 3 == 0) {
					currentPos++
				}
				currentPos++

				if (currentPos >= offset) {
					val originalPos = raw.length - index
					return originalPos
				}
			}

			return raw.length
		}
	}

	TransformedText(AnnotatedString(formatted), offsetMapping)
}
