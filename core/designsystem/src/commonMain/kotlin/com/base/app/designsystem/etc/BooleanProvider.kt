package com.hand.log.designsystem.etc

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanProvider : PreviewParameterProvider<Boolean> {
	override val values = sequenceOf(
		true,
		false,
	)
}
