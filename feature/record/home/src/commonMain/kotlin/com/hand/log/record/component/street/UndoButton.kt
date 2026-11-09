package com.hand.log.record.component.street

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_undo
import handylog.core.res.generated.resources.undo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UndoButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.End,
	) {
		Row(
			modifier = Modifier
				.clip(RoundedCornerShape(6.dp))
				.background(colors.muted)
				.clickable(onClick = onClick)
				.padding(horizontal = 12.dp, vertical = 6.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Icon(
				painter = painterResource(Res.drawable.undo),
				contentDescription = null,
				modifier = Modifier.size(14.dp),
				tint = colors.textSecondary,
			)
			Text(
				text = stringResource(Res.string.btn_undo),
				style = HandyTheme.typography.medium12,
				color = colors.textSecondary,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun UndoButtonPreview() {
	ThemePreview {
		UndoButton(onClick = {})
	}
}
