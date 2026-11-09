package com.hand.log.record.quick.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_confirm
import handylog.core.res.generated.resources.quick_record_guide_call
import handylog.core.res.generated.resources.quick_record_guide_desc
import handylog.core.res.generated.resources.quick_record_guide_hero
import handylog.core.res.generated.resources.quick_record_guide_jump
import handylog.core.res.generated.resources.quick_record_guide_raise
import handylog.core.res.generated.resources.quick_record_guide_result
import handylog.core.res.generated.resources.quick_record_guide_showdown
import handylog.core.res.generated.resources.quick_record_guide_street
import handylog.core.res.generated.resources.quick_record_guide_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class GuideRow(
	val keys: String,
	val output: String,
	val description: StringResource,
)

private val guideRows = listOf(
	GuideRow("UTG … BTN", "BTN ▌", Res.string.quick_record_guide_jump),
	GuideRow("Hero ▾ BTN · 5  5  ⏎", "BTN (Hero) 55", Res.string.quick_record_guide_hero),
	GuideRow(
		"A♠ K CO 3 Raise ⏎",
		"CO opens to 3BB",
		Res.string.quick_record_guide_raise,
	),
	GuideRow("Call ⏎", "calls 3BB", Res.string.quick_record_guide_call),
	GuideRow("A  K  5  ⏎", "[Flop] A K 5", Res.string.quick_record_guide_street),
	GuideRow("Q ♣ Q ♦ · Skip", "UTG: Q♣ Q♦", Res.string.quick_record_guide_showdown),
	GuideRow("Save", "[Result] +14BB", Res.string.quick_record_guide_result),
)

@Composable
internal fun SyntaxGuideSheet(onDismiss: () -> Unit) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.quick_record_guide_title),
		confirmText = stringResource(Res.string.btn_confirm),
		onConfirm = onDismiss,
	) {
		SyntaxGuideContent()
	}
}

@Composable
private fun SyntaxGuideContent() {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = stringResource(Res.string.quick_record_guide_desc),
			style = HandyTheme.typography.regular14,
			color = HandyTheme.colorScheme.textSecondary,
		)
		guideRows.forEach { row ->
			GuideRowItem(
				row = row,
				description = stringResource(row.description),
			)
		}
	}
}

@Composable
private fun GuideRowItem(row: GuideRow, description: String) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(HandyTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalArrangement = Arrangement.spacedBy(2.dp),
	) {
		Text(
			text = row.keys,
			style = HandyTheme.typography.bold14,
			color = HandyTheme.colorScheme.primary,
		)
		Text(
			text = "→ ${row.output}",
			style = HandyTheme.typography.medium14,
			color = HandyTheme.colorScheme.textPrimary,
		)
		Text(
			text = description,
			style = HandyTheme.typography.regular12,
			color = HandyTheme.colorScheme.textSecondary,
		)
	}
}

@ThemePreviews
@Composable
private fun SyntaxGuideContentPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			SyntaxGuideContent()
		}
	}
}
