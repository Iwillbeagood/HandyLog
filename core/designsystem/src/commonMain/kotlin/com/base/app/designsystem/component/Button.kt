package com.hand.log.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import base_app.core.res.generated.resources.Res
import base_app.core.res.generated.resources.btn_complete
import com.hand.log.designsystem.etc.BooleanProvider
import com.hand.log.designsystem.etc.MultipleEventsCutter
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.etc.get
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HmmTheme
import com.hand.log.designsystem.theme.nonScaledSp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun HmLargeButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HmmTheme.typography.bold20,
	containerColor: Color = MaterialTheme.colorScheme.primary,
	contentColor: Color = MaterialTheme.colorScheme.onPrimary,
	enabled: Boolean = true,
	verticalPadding: Dp = 14.dp,
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }

	Button(
		shape = RectangleShape,
		colors = ButtonDefaults.buttonColors(
			containerColor = containerColor,
			contentColor = contentColor,
			disabledContainerColor = MaterialTheme.colorScheme.secondary,
			disabledContentColor = MaterialTheme.colorScheme.onSecondary,
		),
		contentPadding = PaddingValues(vertical = verticalPadding),
		enabled = enabled,
		onClick = { multipleEventsCutter.processEvent(onClick) },
		modifier = modifier.fillMaxWidth(),
	) {
		Text(
			text = text,
			style = textStyle,
		)
	}
}

@Composable
fun RegularButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HmmTheme.typography.bold18,
	containerColor: Color = MaterialTheme.colorScheme.primary,
	contentColor: Color = MaterialTheme.colorScheme.onPrimary,
	enabled: Boolean = true,
	isPreventMultipleClicks: Boolean = true,
	borderStroke: Dp = 4.dp,
	horizontalPadding: Dp = 4.dp,
	verticalPadding: Dp = 10.dp,
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }

	Button(
		shape = RoundedCornerShape(borderStroke),
		colors = ButtonDefaults.buttonColors(
			containerColor = containerColor,
			contentColor = contentColor,
			disabledContainerColor = MaterialTheme.colorScheme.secondary,
			disabledContentColor = MaterialTheme.colorScheme.onSecondary,
		),
		contentPadding = PaddingValues(vertical = verticalPadding, horizontal = horizontalPadding),
		enabled = enabled,
		onClick = {
			if (isPreventMultipleClicks) {
				multipleEventsCutter.processEvent(onClick)
			} else {
				onClick()
			}
		},
		modifier = modifier,
	) {
		Text(
			text = text,
			style = textStyle,
		)
	}
}

@Composable
fun BorderRegularButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HmmTheme.typography.bold18,
	contentColor: Color = MaterialTheme.colorScheme.onSurface,
	borderColor: Color = MaterialTheme.colorScheme.outline,
	enabled: Boolean = true,
	borderStroke: Dp = 4.dp,
	contentPadding: PaddingValues = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }

	OutlinedButton(
		shape = RoundedCornerShape(borderStroke),
		colors = ButtonDefaults.outlinedButtonColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer,
			contentColor = contentColor,
			disabledContainerColor = MaterialTheme.colorScheme.surface,
		),
		border = BorderStroke(1.dp, borderColor),
		contentPadding = contentPadding,
		enabled = enabled,
		onClick = { multipleEventsCutter.processEvent(onClick) },
		modifier = modifier,
	) {
		Text(
			text = text,
			style = textStyle,
		)
	}
}

@Composable
fun BorderSmallButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	color: Color = MaterialTheme.colorScheme.primary,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HmmTheme.typography.medium14,
	enabled: Boolean = true,
	paddingValues: PaddingValues = PaddingValues(horizontal = 5.dp, vertical = 3.dp),
) {
	Box(
		modifier = modifier
			.border(1.dp, color, RoundedCornerShape(6.dp))
			.clickableSingle(
				onClick = onClick,
				enabled = enabled,
			),
	) {
		Text(
			text = text,
			style = textStyle,
			color = color,
			modifier = Modifier
				.padding(paddingValues)
				.align(Alignment.Center),
		)
	}
}

@Composable
fun HmFilledButton(
	text: String,
	enabled: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	textStyle: TextStyle = HmmTheme.typography.regular16,
) {
	val borderColor = if (enabled) Color.Transparent else MaterialTheme.colorScheme.outline

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(20.dp))
			.border(1.dp, borderColor, RoundedCornerShape(20.dp))
			.background(
				if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
			)
			.clickableSingle(onClick = onClick, enabled = enabled),
	) {
		Text(
			text = text,
			style = textStyle,
			color = MaterialTheme.colorScheme.onPrimary,
			modifier = Modifier
				.padding(horizontal = 12.dp, vertical = 6.dp)
				.align(Alignment.Center),
		)
	}
}

@Composable
fun HmOutlinedButton(
	text: String,
	enabled: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	textStyle: TextStyle = HmmTheme.typography.regular16,
	borderColor: Color = MaterialTheme.colorScheme.onSurface,
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(20.dp))
			.border(
				1.dp,
				if (enabled) borderColor else MaterialTheme.colorScheme.outline,
				RoundedCornerShape(20.dp),
			)
			.background(MaterialTheme.colorScheme.surface)
			.clickableSingle(onClick = onClick, enabled = enabled),
	) {
		Text(
			text = text,
			style = textStyle,
			color = MaterialTheme.colorScheme.outline,
			modifier = Modifier
				.padding(horizontal = 12.dp, vertical = 6.dp)
				.align(Alignment.Center),
		)
	}
}

@Composable
fun TextButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	color: Color = MaterialTheme.colorScheme.primary,
	style: TextStyle = HmmTheme.typography.bold16,
) {
	Box(
		modifier = modifier
			.height(50.dp)
			.clickableSingle(onClick),
	) {
		Text(
			text = text,
			style = style.nonScaledSp,
			color = color,
			modifier = Modifier
				.padding(horizontal = 10.dp)
				.align(Alignment.Center),
		)
	}

}

@ThemePreviews
@Composable
private fun LargeButtonPreview(
	@PreviewParameter(BooleanProvider::class) enable: Boolean,
) {
	HandLogTheme {
		HmLargeButton(
			onClick = {},
			enabled = enable,
		)
	}
}

@ThemePreviews
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

@ThemePreviews
@Composable
private fun RegularBorderButtonPreview(
	@PreviewParameter(BooleanProvider::class) enabled: Boolean,
) {
	HandLogTheme {
		BorderRegularButton(
			onClick = {},
			enabled = enabled,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun BorderButtonPreview() {
	HandLogTheme {
		BorderSmallButton(
			onClick = {},
		)
	}
}

@ThemePreviews
@Composable
fun OutlinedButtonPreview() {
	HandLogTheme {
		HmOutlinedButton(
			text = "당상",
			enabled = true,
			onClick = {},
		)
	}
}

@Composable
@ThemePreviews
fun FilledButtonPreview() {
	HandLogTheme {
		HmFilledButton(
			text = "당상",
			enabled = true,
			onClick = {},
		)
	}
}
