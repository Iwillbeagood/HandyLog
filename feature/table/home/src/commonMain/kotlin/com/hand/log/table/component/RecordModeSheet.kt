package com.hand.log.table.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.table.contract.RecordMode
import com.hand.log.ui.SheetOptionCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.quick_record_mode_step
import handylog.core.res.generated.resources.quick_record_mode_step_desc
import handylog.core.res.generated.resources.quick_record_mode_text
import handylog.core.res.generated.resources.quick_record_mode_text_desc
import handylog.core.res.generated.resources.quick_record_mode_title
import handylog.core.res.generated.resources.quick_record_new
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecordModeSheet(
	onModeSelected: (RecordMode) -> Unit,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.quick_record_mode_title),
	) {
		RecordModeOptions(onModeSelected = onModeSelected)
	}
}

@Composable
private fun RecordModeOptions(onModeSelected: (RecordMode) -> Unit) {
	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		SheetOptionCard(
			icon = Res.drawable.grid_3x3,
			title = stringResource(Res.string.quick_record_mode_step),
			description = stringResource(Res.string.quick_record_mode_step_desc),
			onClick = { onModeSelected(RecordMode.STEP) },
		)
		SheetOptionCard(
			icon = Res.drawable.file_text,
			title = stringResource(Res.string.quick_record_mode_text),
			description = stringResource(Res.string.quick_record_mode_text_desc),
			onClick = { onModeSelected(RecordMode.QUICK) },
			badge = stringResource(Res.string.quick_record_new),
		)
	}
}

@ThemePreviews
@Composable
private fun RecordModeOptionsPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			RecordModeOptions(onModeSelected = {})
		}
	}
}
