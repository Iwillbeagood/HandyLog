package com.hand.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hand.log.designsystem.component.HandyTextField
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.showdown_memo
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemoField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	label: String = stringResource(Res.string.showdown_memo),
	minLines: Int = 4,
	onDone: (() -> Unit)? = null,
) {
	HandyTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		label = label,
		minLines = minLines,
		onDone = onDone,
	)
}
