package com.hand.log.handdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.handdetail.contract.HandReviewStatus
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.hand_review_cta
import handylog.core.res.generated.resources.hand_review_error
import handylog.core.res.generated.resources.hand_review_loading
import handylog.core.res.generated.resources.hand_review_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandReviewSection(
	status: HandReviewStatus,
	text: String,
	onRequestReview: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	when (status) {
		HandReviewStatus.IDLE -> Box(
			modifier = modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(10.dp))
				.border(1.dp, colors.primary, RoundedCornerShape(10.dp))
				.clickable(onClick = onRequestReview)
				.padding(vertical = 14.dp),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = stringResource(Res.string.hand_review_cta),
				style = HandyTheme.typography.bold14,
				color = colors.primary,
			)
		}

		HandReviewStatus.LOADING -> Row(
			modifier = modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			CircularProgressIndicator(
				modifier = Modifier.size(16.dp),
				color = colors.primary,
				strokeWidth = 2.dp,
			)
			Text(
				text = stringResource(Res.string.hand_review_loading),
				style = HandyTheme.typography.medium14,
				color = colors.textSecondary,
			)
		}

		HandReviewStatus.LOADED -> Column(
			modifier = modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card)
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			Text(
				text = stringResource(Res.string.hand_review_title),
				style = HandyTheme.typography.bold14,
				color = colors.primary,
			)
			Text(
				text = text,
				style = HandyTheme.typography.regular14,
				color = colors.textPrimary,
			)
		}

		HandReviewStatus.ERROR -> Column(
			modifier = modifier,
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Text(
				text = stringResource(Res.string.hand_review_error),
				style = HandyTheme.typography.medium12,
				color = colors.error,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth(),
			)
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(10.dp))
					.border(1.dp, colors.primary, RoundedCornerShape(10.dp))
					.clickable(onClick = onRequestReview)
					.padding(vertical = 14.dp),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = stringResource(Res.string.hand_review_cta),
					style = HandyTheme.typography.bold14,
					color = colors.primary,
				)
			}
		}
	}
}

@ThemePreviews
@Composable
private fun HandReviewSectionIdlePreview() {
	ThemePreview {
		HandReviewSection(
			status = HandReviewStatus.IDLE,
			text = "",
			onRequestReview = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun HandReviewSectionLoadedPreview() {
	ThemePreview {
		HandReviewSection(
			status = HandReviewStatus.LOADED,
			text = "[좋았던 점] 턴에서 탑투페어로 체크레이즈를 시도한 것은 밸류를 극대화하고 드로우를 차단하는 좋은 플레이였습니다.\n\n[아쉬운 점] 플랍에서 에이스가 떴을 때 체크 대신 리드 베팅을 고려해 볼 수 있었습니다.",
			onRequestReview = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}
