package com.hand.log.handdetail.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.ui.MemoField
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import handylog.core.res.generated.resources.showdown_memo
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoEditSheet(
	initialMemo: String,
	onSave: (String) -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var memo by remember { mutableStateOf(initialMemo) }

	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.showdown_memo),
		confirmText = stringResource(Res.string.btn_complete),
		onConfirm = { onSave(memo) },
		modifier = modifier,
	) {
		MemoField(
			value = memo,
			onValueChange = { memo = it },
			label = stringResource(Res.string.showdown_memo),
		)
	}
}
