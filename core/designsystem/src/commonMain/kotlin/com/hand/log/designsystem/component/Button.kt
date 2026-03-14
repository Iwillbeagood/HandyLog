package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.BooleanProvider
import com.hand.log.designsystem.etc.MultipleEventsCutter
import com.hand.log.designsystem.etc.get
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun RegularButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HandyTheme.typography.bold14,
	containerColor: Color = HandyTheme.colorScheme.primary,
	contentColor: Color = HandyTheme.colorScheme.onPrimary,
	enabled: Boolean = true,
	isPreventMultipleClicks: Boolean = true,
	borderStroke: Dp = 8.dp,
	horizontalPadding: Dp = 4.dp,
	verticalPadding: Dp = 10.dp,
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }
	val colors = HandyTheme.colorScheme
	val bgColor = if (enabled) containerColor else colors.secondary
	val fgColor = if (enabled) contentColor else colors.onSecondary
	val shape = RoundedCornerShape(borderStroke)

	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(shape)
			.background(bgColor, shape)
			.then(
				if (enabled) {
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
		Text(
			text = text,
			style = textStyle,
			color = fgColor,
			textAlign = TextAlign.Center,
		)
	}
}

@Preview
@Composable
private fun RegularButtonPreview(
	@PreviewParameter(BooleanProvider::class) enable: Boolean,
) {
	HandLogTheme {
		RegularButton(
			onClick = {},
			enabled = enable,
			modifier = Modifier.padding(16.dp),
		)
	}
}
