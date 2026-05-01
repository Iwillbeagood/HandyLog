package com.hand.log.handdetail.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.ui.MemoField
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import handylog.core.res.generated.resources.showdown_memo
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoEditSheet(
	memo: String,
	onMemoChange: (String) -> Unit,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.showdown_memo),
		confirmText = stringResource(Res.string.btn_complete),
		onConfirm = onConfirm,
		modifier = modifier,
	) {
		MemoField(
			value = memo,
			onValueChange = onMemoChange,
			label = stringResource(Res.string.showdown_memo),
		)
	}
}
