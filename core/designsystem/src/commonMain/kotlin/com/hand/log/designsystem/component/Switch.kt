package com.hand.log.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.BooleanProvider
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
fun HandySwitch(
	checked: Boolean,
	modifier: Modifier = Modifier,
	text: String? = null,
	enabled: Boolean = true,
	thumbColor: Color = HandyTheme.colorScheme.onPrimary,
	onCheckedChange: ((Boolean) -> Unit)?,
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		if (text != null) {
			Text(
				text = text,
				style = HandyTheme.typography.bold12,
				color = if (checked) HandyTheme.colorScheme.primary else HandyTheme.colorScheme.textSecondary,
			)
		}
		CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
			Switch(
				checked = checked,
				enabled = enabled,
				colors = SwitchDefaults.colors(
					checkedThumbColor = thumbColor,
					uncheckedThumbColor = thumbColor,
					checkedTrackColor = HandyTheme.colorScheme.primary,
					uncheckedTrackColor = HandyTheme.colorScheme.secondary,
					checkedBorderColor = HandyTheme.colorScheme.primary,
					uncheckedBorderColor = HandyTheme.colorScheme.secondary,
				),
				thumbContent = {
					Canvas(modifier = Modifier.fillMaxSize()) {
						drawCircle(
							color = thumbColor,
							radius = 30f,
							center = center,
						)
					}
				},
				onCheckedChange = onCheckedChange,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun SwitchPreview(
	@PreviewParameter(BooleanProvider::class) checked: Boolean,
) {
	ThemePreview {
		HandySwitch(
			checked = checked,
		) {

		}
	}
}
