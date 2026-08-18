package com.hand.log.preflop.quiz.session.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.FadeAnimatedContent
import com.hand.log.designsystem.component.HandyHorizontalDivider
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.preflop.PreflopAction
import com.hand.log.preflop.quiz.session.contract.PreflopQuizSessionState
import com.hand.log.preflop.quiz.session.contract.QuizPhase
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quiz_plan_prompt_vs_3bet
import handylog.core.res.generated.resources.quiz_plan_prompt_vs_4bet
import handylog.core.res.generated.resources.quiz_prev_question
import handylog.core.res.generated.resources.quiz_progress
import handylog.core.res.generated.resources.quiz_prompt
import handylog.core.res.generated.resources.quiz_skip
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuestionContent(
	state: PreflopQuizSessionState,
	onPrimarySelect: (PreflopAction) -> Unit,
	onPlanSelect: (PreflopAction) -> Unit,
	onSkip: () -> Unit,
	onPrevious: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val question = state.current ?: return

	val villainAction = villainActionLabel(question.scenario)

	Column(modifier = Modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(4.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(colors.muted),
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth(state.progress)
						.height(4.dp)
						.clip(RoundedCornerShape(2.dp))
						.background(colors.primary),
				)
			}
			Text(
				text = stringResource(Res.string.quiz_progress, state.index + 1, state.total),
				style = HandyTheme.typography.medium12,
				color = colors.textSecondary,
			)

			SpotChip(question.stack.label)

			Text(
				text = situationDescription(question),
				style = HandyTheme.typography.regular14,
				color = colors.textSecondary,
				modifier = Modifier.fillMaxWidth(),
			)

			QuizPokerTable(
				hero = question.hero,
				villain = question.villain,
				villainAction = villainAction,
				heroAction = heroActionLabel(question.scenario),
				hand = question.hand,
				villainColor = colors.error,
			)

			FadeAnimatedContent(targetState = state.pendingPrimary) { pending ->
				if (pending == null) {
					AnswerStep(
						prompt = stringResource(Res.string.quiz_prompt),
						options = question.options,
						label = { answerText(it) },
						onSelect = onPrimarySelect,
					)
				} else {
					AnswerStep(
						prompt = stringResource(planPromptRes(pending)),
						options = question.planOptions,
						label = { planText(it) },
						onSelect = onPlanSelect,
					)
				}
			}
		}

		Column(modifier = Modifier.fillMaxWidth().background(colors.card)) {
			HandyHorizontalDivider()
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				FadeAnimatedContent(state.canGoPrevious, modifier = Modifier.weight(1f)) { canPrev ->
					if (canPrev) {
						RegularButton(
							onClick = onPrevious,
							text = stringResource(Res.string.quiz_prev_question),
							textStyle = HandyTheme.typography.bold14,
							containerColor = colors.muted,
							contentColor = colors.textPrimary,
							borderStroke = 10.dp,
							verticalPadding = 10.dp,
						)
					} else {
						Spacer(Modifier.fillMaxWidth())
					}
				}
				RegularButton(
					onClick = onSkip,
					modifier = Modifier.weight(1f),
					text = stringResource(Res.string.quiz_skip),
					textStyle = HandyTheme.typography.bold14,
					borderStroke = 10.dp,
					verticalPadding = 10.dp,
				)
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerStep(
	prompt: String,
	options: List<PreflopAction>,
	label: @Composable (PreflopAction) -> String,
	onSelect: (PreflopAction) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
		Text(
			text = prompt,
			style = HandyTheme.typography.medium14,
			color = colors.textSecondary,
			modifier = Modifier.fillMaxWidth(),
		)
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			options.forEach { answer ->
				AnswerButton(label(answer), onClick = { onSelect(answer) })
			}
		}
	}
}

@Composable
private fun AnswerButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(colors.muted)
			.border(1.dp, colors.border, RoundedCornerShape(12.dp))
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 14.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = HandyTheme.typography.bold14,
			color = colors.textPrimary,
		)
	}
}

/** 2차 선택 프롬프트 — 오픈 후엔 3벳, 3벳 후엔 4벳을 당한 상황을 묻는다. */
private fun planPromptRes(primary: PreflopAction) = when (primary) {
	PreflopAction.THREE_BET -> Res.string.quiz_plan_prompt_vs_4bet
	else -> Res.string.quiz_plan_prompt_vs_3bet
}

@ThemePreviews
@Composable
private fun QuestionContentRfiPreview() {
	ThemePreview {
		QuestionContent(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewQuestion),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
		)
	}
}

@ThemePreviews
@Composable
private fun QuestionContentSbLimpPreview() {
	ThemePreview {
		QuestionContent(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewSbLimpQuestion),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
		)
	}
}

@ThemePreviews
@Composable
private fun QuestionContentFacingRfiPreview() {
	ThemePreview {
		QuestionContent(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewFacingQuestion),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
		)
	}
}

@ThemePreviews
@Composable
private fun QuestionContentVs3betPreview() {
	ThemePreview {
		QuestionContent(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewVs3betQuestion),
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
		)
	}
}

@ThemePreviews
@Composable
private fun QuestionContentPlanStepPreview() {
	ThemePreview {
		QuestionContent(
			state = PreflopQuizSessionState(
				phase = QuizPhase.PLAYING,
				questions = listOf(previewQuestion),
				pendingPrimary = PreflopAction.RAISE,
			),
			onPrimarySelect = {},
			onPlanSelect = {},
			onSkip = {},
			onPrevious = {},
		)
	}
}
