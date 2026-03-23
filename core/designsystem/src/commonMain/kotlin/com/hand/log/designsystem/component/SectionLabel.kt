package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandyTheme

@Composable
fun HandySectionLabel(
	text: String,
	modifier: Modifier = Modifier,
	content: @Composable (() -> Unit)? = null,
) {
	if (text.isEmpty()) return

	Column(
		modifier = modifier.fillMaxWidth(),
	) {
		Text(
			text = text,
			style = HandyTheme.typography.regular10,
			color = HandyTheme.colorScheme.textSecondary,
			modifier = Modifier.padding(bottom = 6.dp),
		)
		content?.let {
			it()
		}
	}
}
