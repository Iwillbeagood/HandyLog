package com.hand.log.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.clickableSingle
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.arrow_left
import handylog.core.res.generated.resources.spade_filled
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
fun HandyTopAppbar(
	modifier: Modifier = Modifier,
	title: String = "",
	titleStyle: TextStyle = HandyTheme.typography.medium16,
	contentColor: Color = HandyTheme.colorScheme.textPrimary,
	containerColor: Color = HandyTheme.colorScheme.background,
	navigationType: TopAppbarType = TopAppbarType.Default,
	onBackEvent: () -> Unit = {},
	iconButton: IconButton? = null,
	endContent: @Composable (() -> Unit)? = null,
	subContent: @Composable (() -> Unit)? = null,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.background(containerColor),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = 50.dp),
		) {
			Box(
				modifier = Modifier.align(Alignment.CenterStart),
			) {
				when (navigationType) {
					TopAppbarType.Main -> {
						HomeLogo(title = title)
					}
					else -> {
						TopAppbarIcon(
							tint = contentColor,
							icon = Res.drawable.arrow_left,
							onClick = onBackEvent,
						)
					}
				}
			}

			if (navigationType != TopAppbarType.Main) {
				Text(
					text = title,
					color = contentColor,
					style = titleStyle,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.align(Alignment.Center),
				)
			}

			if (iconButton != null) {
				TopAppbarIconButton(
					iconButton = iconButton,
					modifier = Modifier
						.align(Alignment.CenterEnd)
						.padding(end = 10.dp),
				)
			} else {
				if (endContent != null) {
					Box(
						modifier = Modifier
							.align(Alignment.CenterEnd)
							.padding(end = 10.dp),
					) {
						endContent()
					}
				}
			}

		}

		if (subContent != null) {
			subContent()
		}
	}
}

@Composable
fun TopAppbarIcon(
	tint: Color,
	icon: DrawableResource,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier = modifier
			.size(50.dp)
			.clip(RoundedCornerShape(4.dp))
			.clickableSingle(onClick = onClick),
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = "menu Icon",
			tint = tint,
			modifier = Modifier.align(Alignment.Center),
		)
	}
}

@Composable
fun TopAppbarIconButton(
	iconButton: IconButton,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Box(
		modifier = modifier
			.clickableSingle(onClick = iconButton.onClick)
			.clip(RoundedCornerShape(6.dp))
			.background(colors.primary),
		contentAlignment = Alignment.Center,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			modifier = Modifier
				.padding(horizontal = 12.dp, vertical = 6.dp),
		) {
			Icon(
				painter = painterResource(iconButton.icon),
				contentDescription = null,
				tint = colors.textPrimary,
				modifier = Modifier.size(14.dp),
			)
			iconButton.text?.let {
				Text(
					text = it,
					style = HandyTheme.typography.medium14,
					color = colors.textPrimary,
				)
			}
		}
	}
}

@Composable
private fun HomeLogo(title: String = "") {
	val colors = HandyTheme.colorScheme

	Row(
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.padding(start = 12.dp)
				.size(28.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(colors.felt),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.spade_filled),
				contentDescription = null,
				tint = colors.primary,
				modifier = Modifier.size(14.dp),
			)
		}
		HorizontalSpacer(8.dp)
		if (title.isNotBlank()) {
			Text(
				text = title,
				style = HandyTheme.typography.medium16,
				color = colors.textPrimary,
			)
		} else {
			Text(
				text = "Handy",
				style = HandyTheme.typography.bold16,
				color = colors.primary,
			)
			Text(
				text = "Log",
				style = HandyTheme.typography.bold16,
				color = colors.textPrimary,
			)
		}
	}
}

sealed interface TopAppbarType {
	data object Default : TopAppbarType
	data object Main : TopAppbarType
}

data class IconButton(
	val text: String? = null,
	val icon: DrawableResource,
	val onClick: () -> Unit,
)

@ThemePreviews
@Composable
private fun DefaultTopAppbarPreview() {
	ThemePreview {
		HandyTopAppbar(
			title = "등록",
			navigationType = TopAppbarType.Default,
		)
	}
}

@ThemePreviews
@Composable
private fun MainTopAppbarPreview() {
	ThemePreview {
		HandyTopAppbar(
			navigationType = TopAppbarType.Main,
		)
	}
}

@ThemePreviews
@Composable
private fun MainTopAppbarWithTitlePreview() {
	ThemePreview {
		HandyTopAppbar(
			title = "플레이어",
			navigationType = TopAppbarType.Main,
		)
	}
}

@ThemePreviews
@Composable
private fun IconButtonTopAppbarPreview() {
	ThemePreview {
		HandyTopAppbar(
			title = "테이블 상세",
			iconButton = IconButton(
				text = "설정",
				icon = Res.drawable.spade_filled,
				onClick = {},
			),
		)
	}
}
