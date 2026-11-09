package com.hand.log.record.component.street

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.record_select_opener
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OpenerSelectionHint(
	modifier: Modifier = Modifier,
) {
	Text(
		text = stringResource(Res.string.record_select_opener),
		style = HandyTheme.typography.medium14,
		color = HandyTheme.colorScheme.textSecondary,
		modifier = modifier.fillMaxWidth(),
		textAlign = TextAlign.Center,
	)
}

@ThemePreviews
@Composable
private fun OpenerSelectionHintPreview() {
	ThemePreview {
		OpenerSelectionHint()
	}
}
