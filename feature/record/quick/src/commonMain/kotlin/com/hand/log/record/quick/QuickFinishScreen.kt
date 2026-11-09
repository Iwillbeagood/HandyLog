package com.hand.log.record.quick

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.modal.ModalButtonRow
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.record.quick.component.QuickFinishContent
import com.hand.log.record.quick.component.QuickRecordPreviewData
import com.hand.log.record.quick.contract.QuickFinishForm
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_save
import handylog.core.res.generated.resources.quick_record_finish_edit_actions
import handylog.core.res.generated.resources.quick_record_finish_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickFinishScreen(
	form: QuickFinishForm,
	onBack: () -> Unit,
	onHeroPositionSelect: (String) -> Unit,
	onCardsClick: (CardOwner) -> Unit,
	onEditActions: () -> Unit,
	onSave: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.quick_record_finish_title),
				onBackEvent = onBack,
			)
		},
	) {
		QuickFinishContent(
			form = form,
			onHeroPositionSelect = onHeroPositionSelect,
			onCardsClick = onCardsClick,
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
		)
		ModalButtonRow(
			confirmText = stringResource(Res.string.btn_save),
			onConfirm = onSave,
			confirmEnabled = form.canSave,
			subText = stringResource(Res.string.quick_record_finish_edit_actions),
			onSub = onEditActions,
			subContentColor = HandyTheme.colorScheme.textPrimary,
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun QuickFinishScreenShowdownPreview() {
	ThemePreview {
		QuickRecordPreviewData.state(QuickRecordPreviewData.reviewInput).finishForm?.let { form ->
			QuickFinishScreen(
				form = form,
				onBack = {},
				onHeroPositionSelect = {},
				onCardsClick = {},
				onEditActions = {},
				onSave = {},
			)
		}
	}
}

@ThemePreviews
@Composable
private fun QuickFinishScreenHeroMissingPreview() {
	ThemePreview {
		QuickRecordPreviewData.state(QuickRecordPreviewData.heroMissingInput).finishForm?.let { form ->
			QuickFinishScreen(
				form = form,
				onBack = {},
				onHeroPositionSelect = {},
				onCardsClick = {},
				onEditActions = {},
				onSave = {},
			)
		}
	}
}
