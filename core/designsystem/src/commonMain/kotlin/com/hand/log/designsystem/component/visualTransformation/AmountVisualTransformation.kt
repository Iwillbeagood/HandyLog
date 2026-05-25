package com.hand.log.designsystem.component.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 금액 형식으로 변환하는 VisualTransformation
 * 형식: 1,000,000 (천 단위 콤마)
 * 소수점이 포함된 경우 정수 부분만 콤마 적용
 */
fun amountVisualTransformation(): VisualTransformation = VisualTransformation { text ->
	val originalText = text.text
	val dotIndex = originalText.indexOf('.')

	val intPart = if (dotIndex >= 0) originalText.substring(0, dotIndex) else originalText
	val decPart = if (dotIndex >= 0) originalText.substring(dotIndex) else ""

	val rawInt = intPart.filter { it.isDigit() }

	if (rawInt.isEmpty() && decPart.isEmpty()) {
		return@VisualTransformation TransformedText(text, OffsetMapping.Identity)
	}

	val formattedInt = if (rawInt.isEmpty()) {
		""
	} else {
		buildString {
			rawInt.reversed().forEachIndexed { index, char ->
				if (index > 0 && index % 3 == 0) {
					append(',')
				}
				append(char)
			}
		}.reversed()
	}

	val formatted = formattedInt + decPart
	val totalCommas = if (rawInt.length > 1) (rawInt.length - 1) / 3 else 0

	val offsetMapping = object : OffsetMapping {
		override fun originalToTransformed(offset: Int): Int {
			if (offset == 0) return 0
			if (dotIndex >= 0 && offset > dotIndex) {
				return offset + totalCommas
			}
			val digitsBeforeOffset = intPart.substring(0, offset.coerceAtMost(intPart.length))
				.count { it.isDigit() }
			if (digitsBeforeOffset == 0) return 0
			val commasBefore = if (digitsBeforeOffset > 0) {
				val remaining = rawInt.length - digitsBeforeOffset
				(rawInt.length - 1) / 3 - (if (remaining > 0) (remaining - 1) / 3 else 0)
			} else {
				0
			}
			return offset + commasBefore
		}

		override fun transformedToOriginal(offset: Int): Int {
			if (offset == 0) return 0
			val intFormatLen = formattedInt.length
			if (offset > intFormatLen) {
				return offset - totalCommas
			}
			var commas = 0
			for (i in 0 until offset.coerceAtMost(formatted.length)) {
				if (formatted[i] == ',') commas++
			}
			return offset - commas
		}
	}

	TransformedText(AnnotatedString(formatted), offsetMapping)
}
