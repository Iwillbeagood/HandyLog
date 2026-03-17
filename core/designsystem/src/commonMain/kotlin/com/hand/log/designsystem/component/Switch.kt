package com.hand.log.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
	enabled: Boolean = true,
	thumbColor: Color = HandyTheme.colorScheme.onPrimary,
	onCheckedChange: ((Boolean) -> Unit)?,
) {
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
			modifier = modifier,
		)
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
