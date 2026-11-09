package com.hand.log.record.quick.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.record.quick.contract.QuickSetup
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_confirm
import handylog.core.res.generated.resources.label_bb
import handylog.core.res.generated.resources.label_sb
import handylog.core.res.generated.resources.quick_record_setup_blinds
import handylog.core.res.generated.resources.quick_record_setup_stack
import handylog.core.res.generated.resources.quick_record_setup_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickSetupSheet(
	setup: QuickSetup,
	onSetupChange: (QuickSetup) -> Unit,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.quick_record_setup_title),
		confirmText = stringResource(Res.string.btn_confirm),
		onConfirm = onConfirm,
		confirmEnabled = setup.isValid,
	) {
		QuickSetupForm(setup = setup, onSetupChange = onSetupChange)
	}
}

@Composable
private fun QuickSetupForm(
	setup: QuickSetup,
	onSetupChange: (QuickSetup) -> Unit,
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		HandySectionLabel(stringResource(Res.string.quick_record_setup_blinds))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			HandyTextField(
				value = setup.sbText,
				onValueChange = { onSetupChange(setup.copy(sbText = it)) },
				label = stringResource(Res.string.label_sb),
				modifier = Modifier.weight(1f),
				keyboardType = KeyboardType.Decimal,
			)
			HandyTextField(
				value = setup.bbText,
				onValueChange = { onSetupChange(setup.copy(bbText = it)) },
				label = stringResource(Res.string.label_bb),
				modifier = Modifier.weight(1f),
				keyboardType = KeyboardType.Decimal,
			)
		}

		HandySectionLabel(
			text = stringResource(Res.string.quick_record_setup_stack),
			modifier = Modifier.padding(top = 8.dp),
		)
		HandyTextField(
			value = setup.stackText,
			onValueChange = { onSetupChange(setup.copy(stackText = it)) },
			label = stringResource(Res.string.label_bb),
			keyboardType = KeyboardType.Decimal,
		)
	}
}

@ThemePreviews
@Composable
private fun QuickSetupFormDefaultPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			QuickSetupForm(setup = QuickSetup(), onSetupChange = {})
		}
	}
}

@ThemePreviews
@Composable
private fun QuickSetupFormFilledPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			QuickSetupForm(
				setup = QuickSetup(sbText = "1000", bbText = "2000", stackText = "150"),
				onSetupChange = {},
			)
		}
	}
}
