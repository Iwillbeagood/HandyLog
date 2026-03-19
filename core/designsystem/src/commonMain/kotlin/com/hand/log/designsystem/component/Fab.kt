package com.hand.log.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.plus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HandyFab(
	onClick: () -> Unit,
	icon: DrawableResource = Res.drawable.plus,
	contentDescription: String? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	FloatingActionButton(
		onClick = onClick,
		containerColor = colors.primary,
		contentColor = colors.onPrimary,
		shape = CircleShape,
		modifier = modifier,
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = contentDescription,
		)
	}
}

@ThemePreviews
@Composable
private fun HandyFabPreview() {
	ThemePreview {
		HandyFab(
			onClick = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}
