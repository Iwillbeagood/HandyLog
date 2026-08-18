package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import handylog.core.res.generated.resources.grid_3x3
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegularButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	text: String = stringResource(Res.string.btn_complete),
	textStyle: TextStyle = HandyTheme.typography.bold18,
	containerColor: Color = HandyTheme.colorScheme.primary,
	contentColor: Color = HandyTheme.colorScheme.onPrimary,
	outlined: Boolean = false,
	leadingIcon: DrawableResource? = null,
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
	// outlined 는 배경 대신 테두리를 그리며, 테두리·콘텐츠 모두 containerColor 를 쓴다.
	val themeColor = if (filled) containerColor else colors.secondary
	val bgColor = if (outlined) Color.Transparent else themeColor
	val fgColor = when {
		outlined -> themeColor
		filled -> contentColor
		else -> colors.onSecondary
	}
	val clickable = enabled && !loading
	val shape = RoundedCornerShape(borderStroke)

	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(shape)
			.background(bgColor, shape)
			.then(if (outlined) Modifier.border(1.dp, themeColor, shape) else Modifier)
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
			Row(
				horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (leadingIcon != null) {
					Icon(
						painter = painterResource(leadingIcon),
						contentDescription = null,
						tint = fgColor,
						modifier = Modifier.size(18.dp),
					)
				}
				Text(
					text = text,
					style = textStyle,
					color = fgColor,
					textAlign = TextAlign.Center,
				)
			}
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

@ThemePreviews
@Composable
private fun RegularButtonOutlinedPreview() {
	ThemePreview {
		RegularButton(
			onClick = {},
			text = "Outlined",
			outlined = true,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun RegularButtonOutlinedWithIconPreview() {
	ThemePreview {
		RegularButton(
			onClick = {},
			text = "Outlined",
			outlined = true,
			leadingIcon = Res.drawable.grid_3x3,
			modifier = Modifier.padding(16.dp),
		)
	}
}
