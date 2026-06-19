package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.draw.clip
import com.hand.log.designsystem.component.visualTransformation.amountVisualTransformation
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.calendar
import handylog.core.res.generated.resources.x
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
fun HandyTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	innerModifier: Modifier = Modifier,
	label: String = "",
	placeholder: String = label,
	leadingIcon: DrawableResource? = null,
	keyboardType: KeyboardType = KeyboardType.Text,
	minLines: Int = 1,
	onDone: (() -> Unit)? = null,
) {
	val colors = HandyTheme.colorScheme
	val typography = HandyTheme.typography
	val density = LocalDensity.current
	val minLineHeight = with(density) { typography.regular14.lineHeight.toDp() }
	val focusManager = LocalFocusManager.current
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val borderColor = if (isFocused) colors.primary else colors.inputBorder

	var textFieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
	LaunchedEffect(value) {
		if (textFieldValue.text != value) {
			textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
		}
	}
	LaunchedEffect(isFocused) {
		if (isFocused) {
			textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
		}
	}

	val visualTransformation = if (keyboardType == KeyboardType.Number) {
		amountVisualTransformation()
	} else {
		VisualTransformation.None
	}

	Column(modifier = modifier.fillMaxWidth()) {
		HandySectionLabel(
			text = label,
		)
		BasicTextField(
			value = textFieldValue,
			onValueChange = { newValue ->
				textFieldValue = newValue
				onValueChange(newValue.text)
			},
			modifier = innerModifier,
			textStyle = typography.regular14.copy(color = colors.textPrimary),
			singleLine = minLines == 1,
			minLines = minLines,
			cursorBrush = SolidColor(colors.primary),
			visualTransformation = visualTransformation,
			keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
			keyboardActions = KeyboardActions(
				onDone = {
					focusManager.clearFocus()
					onDone?.invoke()
				},
			),
			interactionSource = interactionSource,
			decorationBox = { innerTextField ->
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.background(colors.muted, RoundedCornerShape(8.dp))
						.border(1.dp, borderColor, RoundedCornerShape(8.dp))
						.padding(horizontal = 12.dp, vertical = 10.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (leadingIcon != null) {
						Icon(
							painter = painterResource(leadingIcon),
							contentDescription = null,
							modifier = Modifier.size(12.dp),
							tint = colors.textSecondary,
						)
						Spacer(modifier = Modifier.width(8.dp))
					}
					Box(modifier = Modifier.weight(1f).defaultMinSize(minHeight = minLineHeight)) {
						if (textFieldValue.text.isEmpty()) {
							Text(
								text = placeholder,
								style = typography.regular14,
								color = colors.textSecondary.copy(alpha = 0.5f),
							)
						}
						innerTextField()
					}

					if (isFocused && textFieldValue.text.isNotEmpty()) {
						Spacer(modifier = Modifier.width(8.dp))
						ScaleInAnimation {
							Icon(
								painter = painterResource(Res.drawable.x),
								contentDescription = "지우기",
								modifier = Modifier
									.size(18.dp)
									.clip(CircleShape)
									.clickable {
										textFieldValue = TextFieldValue()
										onValueChange("")
									}
									.padding(2.dp),
								tint = colors.textSecondary,
							)
						}
					}
				}
			},
		)
	}
}

@ThemePreviews
@Composable
private fun HandyTextFieldEmptyPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			HandyTextField(
				value = "",
				onValueChange = {},
				label = "날짜 (YYYY-MM-DD)",
				leadingIcon = Res.drawable.calendar,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun HandyTextFieldFilledPreview() {
	ThemePreview {
		Column(modifier = Modifier.padding(16.dp)) {
			HandyTextField(
				value = "200000",
				onValueChange = {},
				label = "시작 스택",
				keyboardType = KeyboardType.Number,
			)
		}
	}
}
