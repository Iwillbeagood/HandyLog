package com.hand.log.preflop.chart.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopStack
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.crown
import handylog.core.res.generated.resources.preflop_table_setup_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PreflopStackSheet(
	stacks: List<PreflopStack>,
	lockedStacks: Set<PreflopStack>,
	onDismiss: () -> Unit,
	onSelect: (PreflopStack) -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.preflop_table_setup_title),
	) {
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			stacks.forEach { stack ->
				StackChip(
					label = stack.label,
					locked = stack in lockedStacks,
					onClick = { onSelect(stack) },
				)
			}
		}
	}
}

@Composable
private fun StackChip(
	label: String,
	locked: Boolean,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(10.dp))
			.background(colors.muted)
			.clickable(onClick = onClick)
			.padding(horizontal = 14.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		Text(
			text = label,
			style = HandyTheme.typography.bold14,
			color = colors.textPrimary,
		)
		if (locked) {
			Icon(
				painter = painterResource(Res.drawable.crown),
				contentDescription = null,
				tint = colors.gold,
				modifier = Modifier.size(14.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun PreflopStackSheetPreview() {
	ThemePreview {
		PreflopStackSheet(
			stacks = PreflopStack.entries,
			lockedStacks = PreflopStack.entries.filterTo(mutableSetOf()) { it != PreflopStack.FREE },
			onDismiss = {},
			onSelect = {},
		)
	}
}
