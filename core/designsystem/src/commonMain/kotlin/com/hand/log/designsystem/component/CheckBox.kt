package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.BooleanProvider
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.check
import org.jetbrains.compose.resources.painterResource

@Composable
fun HandyCheckBox(
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	text: String = "",
	checked: Boolean = false,
	enabled: Boolean = true,
) {
	val boxShape = RoundedCornerShape(2.11.dp)
	val backgroundColor = when {
		!enabled -> HandyTheme.colorScheme.secondary
		checked -> HandyTheme.colorScheme.primary
		else -> Color.Transparent
	}
	val borderColor = when {
		!enabled -> HandyTheme.colorScheme.inputBorder
		checked -> HandyTheme.colorScheme.primary
		else -> HandyTheme.colorScheme.inputBorder
	}
	val interactionSource = remember { MutableInteractionSource() }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier,
	) {
		Box(
			modifier = Modifier
				.size(19.dp)
				.clip(boxShape)
				.background(backgroundColor, boxShape)
				.border(1.5.dp, borderColor, boxShape)
				.toggleable(
					value = checked,
					enabled = enabled,
					role = Role.Checkbox,
					interactionSource = interactionSource,
					indication = null,
				) { onCheckedChange(it) },
			contentAlignment = Alignment.Center,
		) {
			if (checked) {
				Icon(
					painter = painterResource(Res.drawable.check),
					contentDescription = null,
					tint = if (enabled) {
						HandyTheme.colorScheme.primary
					} else {
						HandyTheme.colorScheme.secondary
					},
				)
			}
		}
		HorizontalSpacer(5.dp)
		if (text.isNotEmpty()) {
			Text(
				text = text,
				style = HandyTheme.typography.medium12,
				color = HandyTheme.colorScheme.textSecondary,
				modifier = Modifier
					.clip(MaterialTheme.shapes.small)
					.clickable { onCheckedChange(!checked) }
					.padding(2.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun CheckBoxPreview(
	@PreviewParameter(BooleanProvider::class) checked: Boolean,
) {
	ThemePreview {
		HandyCheckBox(
			checked = checked,
			text = "가입자 정보 동일",
			onCheckedChange = {

			},
		)
	}
}

@ThemePreviews
@Composable
private fun EnableCheckBoxPreview() {
	ThemePreview {
		HandyCheckBox(
			checked = true,
			enabled = false,
			text = "가입자 정보 동일",
			onCheckedChange = {

			},
		)
	}
}
