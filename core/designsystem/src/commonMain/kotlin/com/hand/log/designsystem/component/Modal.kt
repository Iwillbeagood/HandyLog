package com.hand.log.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HmmTheme
import com.hand.log.res.R
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModalButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	isLeft: Boolean = false,
) {
	RegularButton(
		text = text,
		onClick = onClick,
		containerColor = if (isLeft) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
		contentColor = if (isLeft) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.surface,
		enabled = enabled,
		modifier = modifier,
	)
}

@Composable
fun BaseDialog(
	title: String = "",
	onDismissRequest: () -> Unit = {},
	verticalPadding: Dp = 10.dp,
	contentSpace: Dp = 20.dp,
	contentPadding: Dp = 16.dp,
	radius: Dp = 4.dp,
	titleTextStyle: TextStyle = HmmTheme.typography.medium20,
	button1: @Composable RowScope.() -> Unit = {},
	button2: (@Composable RowScope.() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit = {},
) {
	Dialog(
		properties = DialogProperties(dismissOnClickOutside = true),
		onDismissRequest = onDismissRequest,
	) {
		Surface(
			shape = RoundedCornerShape(radius),
			modifier = Modifier.fillMaxWidth(),
		) {
			Column(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface)
					.padding(vertical = verticalPadding),
			) {
				if (title.isNotEmpty()) {
					Text(
						text = title,
						style = titleTextStyle,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.fillMaxWidth()
							.padding(bottom = 10.dp),
					)
					HorizontalDivider()
					Spacer(modifier = Modifier.height(contentSpace))
				}
				Column(
					modifier = Modifier
						.padding(horizontal = contentPadding),
				) {
					content()
					Spacer(modifier = Modifier.height(contentSpace))
					Row {
						button1()
						button2?.let {
							Spacer(modifier = Modifier.width(10.dp))
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
	dismissText: String = stringResource(id = R.string.btn_no),
	confirmText: String = stringResource(id = R.string.btn_complete),
	onDismissClick: () -> Unit = onDismissRequest,
	onConfirmClick: () -> Unit = {},
	confirmEnabled: Boolean = true,
	title: String = "",
	contentSpace: Dp = 20.dp,
	contentPadding: Dp = 16.dp,
	verticalPadding: Dp = 10.dp,
	button1Weight: Float = 5f,
	button2Weight: Float = 5f,
	content: @Composable ColumnScope.() -> Unit,
) {
	BaseDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		contentSpace = contentSpace,
		contentPadding = contentPadding,
		verticalPadding = verticalPadding,
		content = content,
		button1 = {
			ModalButton(
				text = dismissText,
				onClick = onDismissClick,
				isLeft = true,
				modifier = Modifier.weight(button1Weight),
			)
		},
		button2 = {
			ModalButton(
				text = confirmText,
				onClick = onConfirmClick,
				enabled = confirmEnabled,
				modifier = Modifier.weight(button2Weight),
			)
		},
	)
}

@Composable
fun HmAlertDialog(
	onDismissRequest: () -> Unit,
	title: String = "",
	content: String = "",
	textStyle: TextStyle = HmmTheme.typography.regular16,
	textAlign: TextAlign = TextAlign.Start,
	contentSpace: Dp = 16.dp,
	verticalPadding: Dp = 10.dp,
	contentPadding: Dp = 16.dp,
	button1Text: String = "확인",
) {
	BaseDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		contentSpace = contentSpace,
		verticalPadding = verticalPadding,
		contentPadding = contentPadding,
		button1 = {
			ModalButton(
				text = button1Text,
				onClick = onDismissRequest,
				modifier = Modifier.fillMaxWidth(),
			)
		},
	) {
		Text(
			text = content,
			style = textStyle,
			textAlign = textAlign,
			modifier = Modifier
				.fillMaxWidth(),
		)
	}
}

@Composable
fun HmAlertGuideDialog(
	onDismissRequest: () -> Unit,
	descriptionText: String,
	onConfirmClick: () -> Unit,
	title: String = "",
	button1Text: String = "취소",
	button2Text: String = "확인",
	button1Weight: Float = 5f,
	button2Weight: Float = 5f,
	isLeft: Boolean = true,
	descriptionStyle: TextStyle = HmmTheme.typography.regular16,
	descriptionTextAlign: TextAlign = TextAlign.Center,
	content: @Composable ColumnScope.() -> Unit = {},
) {
	BaseDialog(
		title = title,
		onDismissRequest = onDismissRequest,
		content = {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				VerticalSpacer(10.dp)
				Text(
					modifier = Modifier
						.fillMaxWidth(),
					text = descriptionText,
					style = descriptionStyle,
					textAlign = descriptionTextAlign,
				)

				content()
			}
		},
		button1 = {
			ModalButton(
				onClick = onDismissRequest,
				text = button1Text,
				modifier = Modifier.weight(button1Weight),
				isLeft = isLeft,
			)
		},
		button2 = {
			ModalButton(
				onClick = onConfirmClick,
				text = button2Text,
				modifier = Modifier.weight(button2Weight),
				isLeft = isLeft.not(),
			)
		},
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
	onDismissRequest: () -> Unit,
	sheetState: SheetState = rememberModalBottomSheetState(true),
	sheetTitle: String = "",
	sheetTitleColor: Color = MaterialTheme.colorScheme.onSurface,
	sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
	sheetContentSpace: Dp = 20.dp,
	horizontalPadding: Dp = 16.dp,
	verticalPadding: Dp = 20.dp,
	sheetContent: @Composable () -> Unit = {},
	sheetButton1: (@Composable RowScope.() -> Unit)? = null,
	sheetButton2: (@Composable RowScope.() -> Unit)? = null,
) {
	ModalBottomSheet(
		sheetState = sheetState,
		sheetMaxWidth = sheetMaxWidth,
		onDismissRequest = onDismissRequest,
		containerColor = MaterialTheme.colorScheme.surface,
		shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
		dragHandle = null,
	) {
		BackHandler(onBack = onDismissRequest)

		Column(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.surface)
				.padding(horizontal = horizontalPadding, vertical = verticalPadding)
				.navigationBarsPadding(),
		) {
			if (sheetTitle.isNotEmpty()) {
				Text(
					text = sheetTitle,
					style = HmmTheme.typography.bold24,
					color = sheetTitleColor,
				)
				Spacer(modifier = Modifier.height(sheetContentSpace))
			}
			sheetContent()
			sheetButton1?.let { button1 ->
				Spacer(modifier = Modifier.height(sheetContentSpace))
				Row {
					button1()
					sheetButton2?.let { button2 ->
						Spacer(modifier = Modifier.width(10.dp))
						button2()
					}
				}
			}

		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonBottomSheet(
	onDismissRequest: () -> Unit,
	sheetTitle: String = "",
	dismissText: String = stringResource(id = R.string.btn_no),
	confirmText: String = stringResource(id = R.string.btn_complete),
	onDismissClick: () -> Unit = onDismissRequest,
	onConfirmClick: () -> Unit = {},
	confirmEnabled: Boolean = true,
	sheetState: SheetState = rememberModalBottomSheetState(true),
	sheetContent: @Composable () -> Unit,
) {
	BaseBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		sheetTitle = sheetTitle,
		sheetContent = sheetContent,
		sheetButton1 = {
			ModalButton(
				text = dismissText,
				onClick = onDismissClick,
				isLeft = true,
				modifier = Modifier.weight(5f),
			)
		},
		sheetButton2 = {
			ModalButton(
				text = confirmText,
				onClick = onConfirmClick,
				enabled = confirmEnabled,
				modifier = Modifier.weight(5f),
			)
		},
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun HmBottomScaffoldPreview() {
	HandLogTheme {
		BaseBottomSheet(
			sheetState = SheetState(true, { 1f }, { 1f }, SheetValue.Expanded, { true }, false),
			sheetTitle = "등록",
			sheetContent = {
				Text(
					text = "등록을 하시겠습니까?",
					style = HmmTheme.typography.medium20,
					color = MaterialTheme.colorScheme.onSurface,
				)
			},
			sheetButton1 = {
				ModalButton(
					text = "아니요",
					onClick = {},
					isLeft = true,
					modifier = Modifier.weight(3f),
				)
			},
			sheetButton2 = {
				ModalButton(
					text = "예",
					onClick = {},
					modifier = Modifier.weight(7f),
				)
			},
			onDismissRequest = { /*TODO*/ },
		)
	}
}

@ThemePreviews
@Composable
private fun HmAlertDialogPreview() {
	HandLogTheme {
		HmAlertDialog(
			title = "생체 정보 변경",
			content = "휴대폰에 등록된 지문이 변경되었습니다.\n다시 지문을 등록해주세요.",
			onDismissRequest = {},
		)
	}
}

@ThemePreviews
@Composable
private fun HmAlertGuideDialogPreview() {
	HandLogTheme {
		HmAlertGuideDialog(
			onDismissRequest = {},
			onConfirmClick = {},
			descriptionText = "휴대폰에 지문이 변경되었습니다.",
			button2Text = "지문 등록하기",
			content = {
				VerticalSpacer(10.dp)
				Text(
					text = "새로 지문을 등록하시겠습니까?",
					style = HmmTheme.typography.bold16,
				)
			},
		)
	}
}
