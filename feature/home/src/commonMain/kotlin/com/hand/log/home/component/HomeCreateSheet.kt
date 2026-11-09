package com.hand.log.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.ui.SheetOptionCard
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.home_create_session
import handylog.core.res.generated.resources.home_create_session_desc
import handylog.core.res.generated.resources.home_create_text_record
import handylog.core.res.generated.resources.home_create_text_record_desc
import handylog.core.res.generated.resources.home_create_title
import handylog.core.res.generated.resources.quick_record_new
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeCreateSheet(
	onCreateTable: () -> Unit,
	onQuickRecord: () -> Unit,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.home_create_title),
	) {
		HomeCreateOptions(onCreateTable = onCreateTable, onQuickRecord = onQuickRecord)
	}
}

@Composable
private fun HomeCreateOptions(
	onCreateTable: () -> Unit,
	onQuickRecord: () -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
		SheetOptionCard(
			icon = Res.drawable.grid_3x3,
			title = stringResource(Res.string.home_create_session),
			description = stringResource(Res.string.home_create_session_desc),
			onClick = onCreateTable,
		)
		SheetOptionCard(
			icon = Res.drawable.file_text,
			title = stringResource(Res.string.home_create_text_record),
			description = stringResource(Res.string.home_create_text_record_desc),
			onClick = onQuickRecord,
			badge = stringResource(Res.string.quick_record_new),
		)
	}
}

@ThemePreviews
@Composable
private fun HomeCreateOptionsPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			HomeCreateOptions(onCreateTable = {}, onQuickRecord = {})
		}
	}
}
