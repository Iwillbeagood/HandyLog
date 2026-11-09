package com.hand.log.record.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandyCheckBox
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.component.modal.ButtonDialog
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.record.contract.RecordStep
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.step_back_description
import handylog.core.res.generated.resources.step_back_skip_warning
import handylog.core.res.generated.resources.step_back_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StepBackConfirmDialog(
	targetStep: RecordStep,
	onConfirm: (RecordStep, Boolean) -> Unit,
	onDismiss: () -> Unit,
) {
	var skipWarning by remember { mutableStateOf(false) }

	ButtonDialog(
		title = stringResource(Res.string.step_back_title),
		onDismissRequest = onDismiss,
		onConfirmClick = { onConfirm(targetStep, skipWarning) },
		onDismissClick = onDismiss,
	) {
		Text(
			text = stringResource(Res.string.step_back_description),
			style = HandyTheme.typography.regular14,
			textAlign = TextAlign.Center,
			color = HandyTheme.colorScheme.textSecondary,
			modifier = Modifier.fillMaxWidth(),
		)
		VerticalSpacer(16.dp)
		HandyCheckBox(
			text = stringResource(Res.string.step_back_skip_warning),
			checked = skipWarning,
			onCheckedChange = { skipWarning = it },
		)
	}
}
