package com.hand.log.record.quick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.TopAppbarIcon
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.record.quick.component.HandHistoryEditor
import com.hand.log.record.quick.component.HandKeyboard
import com.hand.log.record.quick.component.QuickContextStrip
import com.hand.log.record.quick.component.QuickRecordPreviewData
import com.hand.log.record.quick.contract.QuickRecordState
import com.hand.log.record.quick.model.HandKey
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.circle_help
import handylog.core.res.generated.resources.quick_record_finish_title
import handylog.core.res.generated.resources.quick_record_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickRecordScreen(
	state: QuickRecordState,
	onBack: () -> Unit,
	onGuideClick: () -> Unit,
	onSetupClick: () -> Unit,
	onHeroClick: () -> Unit,
	onKeyClick: (HandKey) -> Unit,
	onFinishClick: () -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.quick_record_title),
				onBackEvent = onBack,
				endContent = {
					TopAppbarIcon(
						icon = Res.drawable.circle_help,
						onClick = onGuideClick,
						tint = HandyTheme.colorScheme.textSecondary,
						iconSize = 22.dp,
					)
				},
			)
		},
	) {
		if (state is QuickRecordState.Editing) {
			QuickRecordContent(
				state = state,
				onSetupClick = onSetupClick,
				onHeroClick = onHeroClick,
				onKeyClick = onKeyClick,
				onFinishClick = onFinishClick,
			)
		}
	}
}

@Composable
private fun QuickRecordContent(
	state: QuickRecordState.Editing,
	onSetupClick: () -> Unit,
	onHeroClick: () -> Unit,
	onKeyClick: (HandKey) -> Unit,
	onFinishClick: () -> Unit,
) {
	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			QuickContextStrip(
				blindsLabel = state.blindsLabel,
				isTournament = state.isTournament,
				stackLabel = state.stackLabel,
				heroLabel = state.heroLabel,
				enabled = state.canEditSetup,
				onSetupClick = onSetupClick,
				onHeroClick = onHeroClick,
			)
			Box(modifier = Modifier.weight(1f)) {
				HandHistoryEditor(lines = state.lines)
			}
		}
		FadeAnimatedContent(targetState = state.isFinishing) { isFinishing ->
			if (isFinishing) {
				RegularButton(
					onClick = onFinishClick,
					text = stringResource(Res.string.quick_record_finish_title),
					textStyle = HandyTheme.typography.bold16,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 12.dp),
				)
			} else {
				HandKeyboard(
					positionRows = state.positionRows,
					enabledKeys = state.enabledKeys,
					actorKey = state.actorKey,
					heroKey = state.heroKey,
					amountInChips = state.amountInChips,
					streetCloseTarget = state.streetCloseTarget,
					sizedAsMultiple = state.sizedAsMultiple,
					onKeyClick = onKeyClick,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun QuickRecordScreenActionPreview() {
	ThemePreview {
		QuickRecordScreen(
			state = QuickRecordPreviewData.state(QuickRecordPreviewData.actionInput),
			onBack = {},
			onGuideClick = {},
			onSetupClick = {},
			onHeroClick = {},
			onKeyClick = {},
			onFinishClick = {},
		)
	}
}

@ThemePreviews
@Composable
private fun QuickRecordScreenDonePreview() {
	ThemePreview {
		QuickRecordScreen(
			state = QuickRecordPreviewData.state(QuickRecordPreviewData.doneInput),
			onBack = {},
			onGuideClick = {},
			onSetupClick = {},
			onHeroClick = {},
			onKeyClick = {},
			onFinishClick = {},
		)
	}
}
