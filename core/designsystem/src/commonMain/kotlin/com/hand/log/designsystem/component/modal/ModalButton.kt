package com.hand.log.designsystem.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.theme.HandyTheme

@Composable
fun ModalButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	isNegative: Boolean = false,
) {
	if (isNegative) {
		RegularButton(
			text = text,
			onClick = onClick,
			containerColor = HandyTheme.colorScheme.secondary,
			contentColor = HandyTheme.colorScheme.onSecondary,
			textStyle = HandyTheme.typography.bold16,
			modifier = modifier,
		)
	} else {
		RegularButton(
			text = text,
			onClick = onClick,
			enabled = enabled,
			textStyle = HandyTheme.typography.bold16,
			modifier = modifier,
		)
	}
}
