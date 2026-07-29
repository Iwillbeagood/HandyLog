package com.hand.log.preflop.quiz.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.TopAppbarType
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.QuizRecord
import com.hand.log.preflop.quiz.common.PreflopQuizType
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.chevron_right
import handylog.core.res.generated.resources.grid_3x3
import handylog.core.res.generated.resources.preflop_quiz_select
import handylog.core.res.generated.resources.preflop_quiz_title
import handylog.core.res.generated.resources.preflop_recent_empty
import handylog.core.res.generated.resources.preflop_recent_title
import handylog.core.res.generated.resources.quiz_type_mixed_desc
import handylog.core.res.generated.resources.quiz_type_mixed_title
import handylog.core.res.generated.resources.quiz_type_rfi_desc
import handylog.core.res.generated.resources.quiz_type_rfi_title
import handylog.core.res.generated.resources.quiz_type_vs3bet_desc
import handylog.core.res.generated.resources.quiz_type_vs3bet_title
import handylog.core.res.generated.resources.quiz_type_vsopen_desc
import handylog.core.res.generated.resources.quiz_type_vsopen_title
import handylog.core.res.generated.resources.trophy
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PreflopQuizScreen(
	records: List<QuizRecord>,
	onBack: () -> Unit,
	onTypeSelect: (PreflopQuizType) -> Unit,
) {
	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.preflop_quiz_title),
				navigationType = TopAppbarType.Default,
				onBackEvent = onBack,
			)
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp),
		) {
			SectionHeader(stringResource(Res.string.preflop_quiz_select))

			Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
				QuizTypeItem(
					icon = Res.drawable.grid_3x3,
					iconColor = HandyTheme.colorScheme.primary,
					title = stringResource(Res.string.quiz_type_rfi_title),
					description = stringResource(Res.string.quiz_type_rfi_desc),
					onClick = { onTypeSelect(PreflopQuizType.RFI) },
				)
				QuizTypeItem(
					icon = Res.drawable.trophy,
					iconColor = HandyTheme.colorScheme.gold,
					title = stringResource(Res.string.quiz_type_vsopen_title),
					description = stringResource(Res.string.quiz_type_vsopen_desc),
					onClick = { onTypeSelect(PreflopQuizType.VS_OPEN) },
				)
				QuizTypeItem(
					icon = Res.drawable.trophy,
					iconColor = HandyTheme.colorScheme.error,
					title = stringResource(Res.string.quiz_type_vs3bet_title),
					description = stringResource(Res.string.quiz_type_vs3bet_desc),
					onClick = { onTypeSelect(PreflopQuizType.VS_3BET) },
				)
				QuizTypeItem(
					icon = Res.drawable.grid_3x3,
					iconColor = HandyTheme.colorScheme.textSecondary,
					title = stringResource(Res.string.quiz_type_mixed_title),
					description = stringResource(Res.string.quiz_type_mixed_desc),
					onClick = { onTypeSelect(PreflopQuizType.MIXED) },
				)
			}

			SectionHeader(stringResource(Res.string.preflop_recent_title))
			RecentRecordsCard(records)
		}
	}
}

@Composable
private fun SectionHeader(text: String) {
	Text(
		text = text,
		style = HandyTheme.typography.bold12,
		color = HandyTheme.colorScheme.textSecondary,
	)
}

@Composable
private fun QuizTypeItem(
	icon: DrawableResource,
	iconColor: Color,
	title: String,
	description: String,
	onClick: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card)
			.clickable(onClick = onClick)
			.padding(16.dp),
		horizontalArrangement = Arrangement.spacedBy(14.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(44.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(iconColor.copy(alpha = 0.15f)),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(icon),
				contentDescription = null,
				modifier = Modifier.size(22.dp),
				tint = iconColor,
			)
		}
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Text(
				text = title,
				style = HandyTheme.typography.bold16,
				color = colors.textPrimary,
			)
			Text(
				text = description,
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
			)
		}
		Icon(
			painter = painterResource(Res.drawable.chevron_right),
			contentDescription = null,
			modifier = Modifier.size(18.dp),
			tint = colors.textSecondary,
		)
	}
}

@Composable
private fun RecentRecordsCard(records: List<QuizRecord>) {
	val colors = HandyTheme.colorScheme
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.background(colors.card),
	) {
		if (records.isEmpty()) {
			Text(
				text = stringResource(Res.string.preflop_recent_empty),
				style = HandyTheme.typography.regular12,
				color = colors.textSecondary,
				modifier = Modifier.padding(16.dp),
			)
			return
		}
		records.forEachIndexed { index, record ->
			val accuracy = if (record.total == 0) 0 else record.score * 100 / record.total
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = quizTypeLabel(record.quizType),
					style = HandyTheme.typography.medium14,
					color = colors.textPrimary,
				)
				Row(
					horizontalArrangement = Arrangement.spacedBy(10.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = "${record.score}/${record.total}",
						style = HandyTheme.typography.bold14,
						color = colors.textPrimary,
					)
					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(10.dp))
							.background(if (accuracy >= 70) colors.primary else colors.gold)
							.padding(horizontal = 8.dp, vertical = 2.dp),
					) {
						Text(
							text = "$accuracy%",
							style = HandyTheme.typography.bold12,
							color = colors.onPrimary,
						)
					}
				}
			}
			if (index < records.lastIndex) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(colors.border),
				)
			}
		}
	}
}

@Composable
private fun quizTypeLabel(typeName: String): String = when (typeName) {
	PreflopQuizType.RFI.name -> stringResource(Res.string.quiz_type_rfi_title)
	PreflopQuizType.VS_OPEN.name -> stringResource(Res.string.quiz_type_vsopen_title)
	PreflopQuizType.VS_3BET.name -> stringResource(Res.string.quiz_type_vs3bet_title)
	else -> stringResource(Res.string.quiz_type_mixed_title)
}

@ThemePreviews
@Composable
private fun PreflopQuizScreenPreview() {
	ThemePreview {
		PreflopQuizScreen(
			records = emptyList(),
			onBack = {},
			onTypeSelect = {},
		)
	}
}
