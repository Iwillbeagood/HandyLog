package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.preflop.quiz.common.PreflopQuizType
import com.hand.log.preflop.quiz.common.formatQuizPlayedAt
import com.hand.log.preflop.quiz.common.titleRes
import com.hand.log.preflop.quiz.session.contract.QuizResult
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.file_text
import handylog.core.res.generated.resources.quiz_result_accuracy
import handylog.core.res.generated.resources.quiz_result_avgtime
import handylog.core.res.generated.resources.quiz_result_avgtime_value
import handylog.core.res.generated.resources.quiz_result_desc
import handylog.core.res.generated.resources.quiz_result_fair
import handylog.core.res.generated.resources.quiz_result_good
import handylog.core.res.generated.resources.quiz_result_great
import handylog.core.res.generated.resources.quiz_result_streak
import handylog.core.res.generated.resources.quiz_result_streak_value
import handylog.core.res.generated.resources.quiz_retry
import handylog.core.res.generated.resources.quiz_start_review
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResultContent(
	result: QuizResult,
	reviewTotal: Int,
	onRetry: () -> Unit,
	onStartReview: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = stringResource(result.quizType.titleRes()),
				style = HandyTheme.typography.bold18,
				color = colors.textPrimary,
			)
			Text(
				text = formatQuizPlayedAt(result.playedAt),
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
		}

		ScoreCircle(result)
		val msg = when {
			result.accuracyPct >= 80 -> Res.string.quiz_result_great
			result.accuracyPct >= 50 -> Res.string.quiz_result_good
			else -> Res.string.quiz_result_fair
		}
		Text(
			text = stringResource(msg),
			style = HandyTheme.typography.bold16,
			color = colors.primary,
		)
		Text(
			text = stringResource(Res.string.quiz_result_desc),
			style = HandyTheme.typography.regular12,
			color = colors.textSecondary,
		)

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.background(colors.card),
		) {
			StatRow(
				stringResource(Res.string.quiz_result_accuracy),
				"${result.accuracyPct}%",
				colors.textPrimary,
			)
			StatRow(
				stringResource(Res.string.quiz_result_avgtime),
				stringResource(Res.string.quiz_result_avgtime_value, seconds(result.avgResponseMs)),
				colors.textPrimary,
			)
			StatRow(
				stringResource(Res.string.quiz_result_streak),
				stringResource(Res.string.quiz_result_streak_value, result.bestStreak),
				colors.gold,
			)
		}

		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			PrimaryButton(text = stringResource(Res.string.quiz_retry), onClick = onRetry)

			if (reviewTotal > 0) {
				SecondaryButton(
					icon = Res.drawable.file_text,
					text = stringResource(Res.string.quiz_start_review),
					onClick = onStartReview,
					modifier = Modifier.fillMaxWidth(),
				)
			}
		}
	}
}

@Composable
private fun SecondaryButton(
	icon: DrawableResource,
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(10.dp))
			.background(colors.muted)
			.clickable(onClick = onClick)
			.padding(vertical = 14.dp),
		horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = null,
			tint = colors.textPrimary,
			modifier = Modifier.size(18.dp),
		)
		Text(
			text = text,
			style = HandyTheme.typography.bold16,
			color = colors.textPrimary,
		)
	}
}

@Composable
private fun ScoreCircle(result: QuizResult) {
	val colors = HandyTheme.colorScheme
	val fraction = result.scoreFraction
	Box(
		modifier = Modifier.size(140.dp),
		contentAlignment = Alignment.Center,
	) {
		val track = colors.muted
		val progress = colors.primary
		Canvas(modifier = Modifier.size(140.dp)) {
			val stroke = 12f
			drawArc(
				color = track,
				startAngle = -90f,
				sweepAngle = 360f,
				useCenter = false,
				style = Stroke(width = stroke),
				size = Size(size.width - stroke, size.height - stroke),
				topLeft = Offset(stroke / 2, stroke / 2),
			)
			drawArc(
				color = progress,
				startAngle = -90f,
				sweepAngle = 360f * fraction,
				useCenter = false,
				style = Stroke(width = stroke),
				size = Size(size.width - stroke, size.height - stroke),
				topLeft = Offset(stroke / 2, stroke / 2),
			)
		}
		Row(verticalAlignment = Alignment.Bottom) {
			Text(
				text = "${result.score}",
				style = HandyTheme.typography.bold32,
				color = colors.textPrimary,
			)
			Text(
				text = " / ${result.total}",
				style = HandyTheme.typography.regular16,
				color = colors.textSecondary,
			)
		}
	}
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 14.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(label, style = HandyTheme.typography.regular12, color = HandyTheme.colorScheme.textSecondary)
		Text(value, style = HandyTheme.typography.bold14, color = valueColor)
	}
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
			.background(HandyTheme.colorScheme.primary)
			.clickable(onClick = onClick)
			.padding(vertical = 14.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold16,
			color = HandyTheme.colorScheme.onPrimary,
		)
	}
}

/** ms → "3.2" 형태의 초 문자열(소수 1자리). */
private fun seconds(ms: Long): String {
	val tenths = (ms + 50) / 100
	return "${tenths / 10}.${tenths % 10}"
}

@ThemePreviews
@Composable
private fun ResultContentPreview() {
	ThemePreview {
		Box(modifier = Modifier.background(HandyTheme.colorScheme.background)) {
			ResultContent(
				result = QuizResult(
					score = 8,
					total = 10,
					accuracyPct = 80,
					avgResponseMs = 3200,
					bestStreak = 5,
					quizType = PreflopQuizType.RFI,
					playedAt = 1_753_800_000_000L,
				),
				reviewTotal = 2,
				onRetry = {},
				onStartReview = {},
			)
		}
	}
}
