package com.hand.log.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HmmTheme
import com.hand.log.res.R

@Composable
fun HmTopAppbar(
	modifier: Modifier = Modifier,
	title: String = "",
	titleStyle: TextStyle = HmmTheme.typography.medium16,
	contentColor: Color = MaterialTheme.colorScheme.onSurface,
	containerColor: Color = MaterialTheme.colorScheme.surface,
	lineColor: Color = MaterialTheme.colorScheme.outlineVariant,
	iconId: Int = R.drawable.ic_back,
	navigationType: HmTopAppbarType = HmTopAppbarType.Default,
	onBackEvent: () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(50.dp)
			.background(containerColor)
			.statusBarsPadding(),
	) {
		Row(
			modifier = modifier.fillMaxSize(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Box(
				modifier = Modifier
					.size(50.dp)
					.clickable(onClick = onBackEvent),
			) {
				TopAppbarIcon(
					tint = contentColor,
					icon = iconId,
					onClick = onBackEvent,
				)
			}
			Text(
				text = title,
				color = contentColor,
				style = titleStyle,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f),
			)

			when (navigationType) {
				is HmTopAppbarType.Custom -> {
					navigationType.content()
				}
				is HmTopAppbarType.IconButton -> {
					TopAppbarIcon(
						tint = contentColor,
						icon = navigationType.icon,
						onClick = navigationType.onClick,
					)
				}
				else -> {}
			}
		}
		HmHorizontalDivider(
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.BottomCenter),
			lineColor = lineColor,
		)
	}
}

@Composable
fun TopAppbarIcon(
	tint: Color,
	@DrawableRes icon: Int,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier = modifier
			.size(50.dp)
			.clickable(onClick = onClick),
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
fun TopAppbarIcon(
	tint: Color,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier = modifier
			.size(50.dp)
			.clickable(onClick = onClick),
	) {
		Icon(
			imageVector = icon,
			contentDescription = "menu Icon",
			tint = tint,
			modifier = Modifier.align(Alignment.Center),
		)
	}
}

sealed interface HmTopAppbarType {
	data object Default : HmTopAppbarType
	data class Custom(val content: @Composable () -> Unit) : HmTopAppbarType
	data class IconButton(val icon: ImageVector, val onClick: () -> Unit) : HmTopAppbarType
}

@ThemePreviews
@Composable
fun BasicTopAppbarPreview() {
	HandLogTheme {
		HmTopAppbar(
			title = "등록",
		)
	}
}

@ThemePreviews
@Composable
fun CustomTopAppbarPreview() {
	HandLogTheme {
		HmTopAppbar(
			title = "정산",
			navigationType = HmTopAppbarType.Custom {
				Text(
					modifier = Modifier
						.padding(end = 5.dp),
					text = "테스트",
				)
			},
		)
	}
}

@ThemePreviews
@Composable
fun IconButtonTopAppbarPreview() {
	HandLogTheme {
		HmTopAppbar(
			title = "정산",
			navigationType = HmTopAppbarType.IconButton(
				icon = Icons.Default.MoreVert,
				onClick = {},
			),
		)
	}
}
