package com.hand.log.designsystem.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_no
import handylog.core.res.generated.resources.btn_yes
import ktc.cargo.driver.designsystem.component.modal.ModalButton
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BaseDialog(
	title: String = "",
	onDismissRequest: () -> Unit = {},
	button1: @Composable RowScope.() -> Unit = {},
	button2: (@Composable RowScope.() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit = {},
) {
	Dialog(
		properties = DialogProperties(dismissOnClickOutside = true),
		onDismissRequest = onDismissRequest,
	) {
		Surface(
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier.fillMaxWidth(),
		) {
			Column(
				modifier = Modifier
					.background(HandyTheme.colorScheme.modalBackground)
					.padding(18.dp),
			) {
				VerticalSpacer(8.dp)
				if (title.isNotEmpty()) {
					Text(
						text = title,
						style = HandyTheme.typography.bold22,
						color = HandyTheme.colorScheme.textPrimary,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth(),
					)
					VerticalSpacer(14.dp)
				}
				Column(
					modifier = Modifier,
				) {
					content()
					VerticalSpacer(24.dp)
					Row {
						button1()
						button2?.let {
							Spacer(modifier = Modifier.width(8.dp))
							it()
						}
					}
				}
			}
		}
	}
}

@Composable
fun ButtonDialog(
	onDismissRequest: () -> Unit,
	title: String = "",
	confirmText: String = stringResource(Res.string.btn_yes),
	dismissText: String = stringResource(Res.string.btn_no),
	onConfirmClick: () -> Unit = {},
	onDismissClick: () -> Unit = onDismissRequest,
	confirmEnabled: Boolean = true,
	isNegative: Boolean = true,
	content: @Composable ColumnScope.() -> Unit,
) {
	BaseDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		content = content,
		button1 = {
			ModalButton(
				text = dismissText,
				onClick = onDismissClick,
				isNegative = isNegative,
				modifier = Modifier.weight(1f),
			)
		},
		button2 = {
			ModalButton(
				text = confirmText,
				onClick = {
					onDismissRequest()
					onConfirmClick()
				},
				enabled = confirmEnabled,
				isNegative = isNegative.not(),
				modifier = Modifier.weight(1f),
			)
		},
	)
}

@Composable
fun DefaultDialog(
	onDismissRequest: () -> Unit,
	title: String = "",
	content: String = "",
	confirmText: String = stringResource(Res.string.btn_yes),
	dismissText: String = stringResource(Res.string.btn_no),
	description: String? = null,
	warning: String? = null,
	isNegative: Boolean = true,
	confirmEnabled: Boolean = true,
	onConfirmClick: () -> Unit = {},
	onDismissClick: () -> Unit = onDismissRequest,
) {
	ButtonDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		confirmText = confirmText,
		dismissText = dismissText,
		onConfirmClick = onConfirmClick,
		onDismissClick = onDismissClick,
		isNegative = isNegative,
		confirmEnabled = confirmEnabled,
	) {
		Text(
			text = content,
			style = HandyTheme.typography.regular16,
			textAlign = TextAlign.Center,
			color = HandyTheme.colorScheme.textPrimary,
			modifier = Modifier.fillMaxWidth(),
		)
		if (description != null) {
			VerticalSpacer(10.dp)
			Text(
				text = description,
				style = HandyTheme.typography.regular14,
				textAlign = TextAlign.Center,
				color = HandyTheme.colorScheme.textSecondary,
				modifier = Modifier.fillMaxWidth(),
			)
		}
		if (warning != null) {
			VerticalSpacer(14.dp)
			Text(
				text = warning,
				style = HandyTheme.typography.bold18,
				textAlign = TextAlign.Center,
				color = HandyTheme.colorScheme.textPrimary,
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}
}

@Composable
fun ConfirmDialog(
	onDismissRequest: () -> Unit,
	title: String = "",
	content: String = "",
	description: String? = null,
	warning: String? = null,
	confirmText: String = stringResource(Res.string.btn_yes),
) {
	BaseDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		button1 = {
			ModalButton(
				text = confirmText,
				onClick = onDismissRequest,
				modifier = Modifier.fillMaxWidth(),
			)
		},
	) {
		Text(
			text = content,
			style = HandyTheme.typography.regular16,
			textAlign = TextAlign.Center,
			color = HandyTheme.colorScheme.textPrimary,
			modifier = Modifier.fillMaxWidth(),
		)
		if (description != null) {
			VerticalSpacer(10.dp)
			Text(
				text = description,
				style = HandyTheme.typography.regular14,
				color = HandyTheme.colorScheme.textSecondary,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth(),
			)
		}
		if (warning != null) {
			VerticalSpacer(14.dp)
			Text(
				text = warning,
				style = HandyTheme.typography.bold18,
				textAlign = TextAlign.Center,
				color = HandyTheme.colorScheme.textPrimary,
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}
}

@Preview
@Composable
private fun DefaultDialogPreview() {
	HandLogTheme {
		DefaultDialog(
			title = "화물등록",
			content = "화물 등록을 하시겠습니까?",
			description = "등록 후 수정이 불가능합니다.",
			onDismissRequest = {},
		)
	}
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
	HandLogTheme {
		ConfirmDialog(
			title = "생체 정보 변경",
			content = "휴대폰에 등록된 지문이 변경되었습니다.\n다시 지문을 등록해주세요.",
			onDismissRequest = {},
		)
	}
}
