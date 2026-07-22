package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.BooleanProvider
import com.hand.log.designsystem.etc.MultipleEventsCutter
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.get
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegularButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HandyTheme.typography.bold18,
	containerColor: Color = HandyTheme.colorScheme.primary,
	contentColor: Color = HandyTheme.colorScheme.onPrimary,
	enabled: Boolean = true,
	loading: Boolean = false,
	isPreventMultipleClicks: Boolean = true,
	borderStroke: Dp = 8.dp,
	horizontalPadding: Dp = 4.dp,
	verticalPadding: Dp = 12.dp,
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }
	val colors = HandyTheme.colorScheme
	// 로딩 중에는 클릭만 막고 활성 색상을 유지해 진행 중임을 드러낸다.
	val filled = enabled || loading
	val bgColor = if (filled) containerColor else colors.secondary
	val fgColor = if (filled) contentColor else colors.onSecondary
	val clickable = enabled && !loading
	val shape = RoundedCornerShape(borderStroke)

	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(shape)
			.background(bgColor, shape)
			.then(
				if (clickable) {
					Modifier.clickable {
						if (isPreventMultipleClicks) {
							multipleEventsCutter.processEvent(onClick)
						} else {
							onClick()
						}
					}
				} else {
					Modifier
				},
			)
			.padding(vertical = verticalPadding, horizontal = horizontalPadding),
		contentAlignment = Alignment.Center,
	) {
		if (loading) {
			CircularProgressIndicator(
				color = fgColor,
				strokeWidth = 2.dp,
				modifier = Modifier.size(20.dp),
			)
		} else {
			Text(
				text = text,
				style = textStyle,
				color = fgColor,
				textAlign = TextAlign.Center,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun RegularButtonPreview(
	@PreviewParameter(BooleanProvider::class) enable: Boolean,
) {
	ThemePreview {
		RegularButton(
			onClick = {},
			enabled = enable,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun RegularButtonLoadingPreview() {
	ThemePreview {
		RegularButton(
			onClick = {},
			loading = true,
			modifier = Modifier.padding(16.dp),
		)
	}
}
