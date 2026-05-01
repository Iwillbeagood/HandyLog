package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.showdown_memo
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoSection(
	memo: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(vertical = 16.dp, horizontal = 12.dp),
	) {
		Text(
			text = stringResource(Res.string.showdown_memo),
			style = typography.bold16,
			color = colors.textPrimary,
		)
		VerticalSpacer(8.dp)
		Text(
			text = memo.ifEmpty { "-" },
			style = typography.regular14,
			color = if (memo.isEmpty()) colors.textSecondary else colors.textPrimary,
		)
	}
}
