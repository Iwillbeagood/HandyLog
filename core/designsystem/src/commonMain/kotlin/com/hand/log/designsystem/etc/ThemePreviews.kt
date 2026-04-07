package com.hand.log.designsystem.etc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HorizontalSpacer
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme

@Preview(name = "Dark | Light", widthDp = 720)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class ThemePreviews

@Composable
fun ThemePreview(content: @Composable () -> Unit) {
	Row(
		modifier = Modifier.fillMaxWidth(),
	) {
		HandLogTheme(darkTheme = true) {
			Column(
				modifier = Modifier
					.weight(1f)
					.background(HandyTheme.colorScheme.background),
			) {
				content()
			}
		}
		HorizontalSpacer(10.dp)
		HandLogTheme(darkTheme = false) {
			Column(
				modifier = Modifier
					.weight(1f)
					.background(HandyTheme.colorScheme.background),
			) {
				content()
			}
		}
	}
}
