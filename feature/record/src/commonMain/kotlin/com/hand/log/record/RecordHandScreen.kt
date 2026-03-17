package com.hand.log.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.designsystem.theme.nonScaledSp
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Blinds
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.PokerTable
import com.hand.log.domain.model.Rank
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.Suit
import com.hand.log.record.component.SetupStepContent
import com.hand.log.record.component.StreetStepContent
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

@Composable
internal fun RecordHandScreen(
	state: RecordHandState.Recording,
	onBack: () -> Unit,
	onSelectHeroCard: () -> Unit,
	onSelectBoardCard: (Street) -> Unit,
	onUpdateHeroStack: (String) -> Unit,
	onUpdateButtonSeat: (Int) -> Unit,
	onUpdateBlinds: (String, String) -> Unit,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onUpdatePlayerStack: (Int, String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	onNextStep: () -> Unit,
	onPreviousStep: () -> Unit,
	onUpdateResult: (String) -> Unit,
	onUpdateMemo: (String) -> Unit,
	onSave: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		containerColor = colors.background,
		topBar = {
			HandyTopAppbar(
				title = "핸드 기록",
				onBackEvent = {
					if (state.currentStep == RecordStep.SETUP) {
						onBack()
					} else {
						onPreviousStep()
					}
				},
			)
		},
		bottomBar = {
			BottomNavigationBar(
				currentStep = state.currentStep,
				canProceed = state.currentStep != RecordStep.SETUP || state.canProceedFromSetup,
				onPrevious = onPreviousStep,
				onNext = onNextStep,
				onSave = onSave,
			)
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			StepIndicator(
				currentStep = state.currentStep,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
			)

			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.weight(1f),
				contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				when (state.currentStep) {
					RecordStep.SETUP -> {
						item {
							SetupStepContent(
								state = state,
								onSelectHeroCard = onSelectHeroCard,
								onUpdateHeroStack = onUpdateHeroStack,
								onUpdateButtonSeat = onUpdateButtonSeat,
								onUpdateBlinds = onUpdateBlinds,
							)
						}
					}

					RecordStep.PREFLOP, RecordStep.FLOP, RecordStep.TURN, RecordStep.RIVER -> {
						item {
							StreetStepContent(
								state = state,
								onSelectBoardCard = onSelectBoardCard,
								onSelectActionSeat = onSelectActionSeat,
								onSelectActionType = onSelectActionType,
								onUpdateActionAmount = onUpdateActionAmount,
								onUpdatePlayerStack = onUpdatePlayerStack,
								onConfirmAction = onConfirmAction,
								onRemoveLastAction = onRemoveLastAction,
								onUpdateResult = onUpdateResult,
								onUpdateMemo = onUpdateMemo,
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun StepIndicator(
	currentStep: RecordStep,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme
	val steps = RecordStep.entries

	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically,
	) {
		steps.forEachIndexed { index, step ->
			val isCurrent = step == currentStep
			val isPassed = step.ordinal < currentStep.ordinal

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.weight(1f),
			) {
				Box(
					modifier = Modifier
						.size(32.dp)
						.clip(CircleShape)
						.background(
							when {
								isCurrent -> colors.primary
								isPassed -> colors.primary.copy(alpha = 0.5f)
								else -> colors.muted
							},
						),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "${index + 1}",
						style = HandyTheme.typography.bold14.nonScaledSp,
						color = if (isCurrent || isPassed) colors.onPrimary else colors.textSecondary,
					)
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = step.label,
					style = HandyTheme.typography.regular12,
					color = if (isCurrent) colors.textPrimary else colors.textSecondary,
					textAlign = TextAlign.Center,
				)
			}
		}
	}
}

// SetupStepContent is in com.hand.log.record.component package

@Composable
private fun BottomNavigationBar(
	currentStep: RecordStep,
	canProceed: Boolean,
	onPrevious: () -> Unit,
	onNext: () -> Unit,
	onSave: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val isFirstStep = currentStep == RecordStep.SETUP
	val isLastStep = currentStep == RecordStep.RIVER
	val isStreetStep = currentStep != RecordStep.SETUP

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(colors.background)
			.padding(horizontal = 16.dp, vertical = 20.dp),
		horizontalArrangement = Arrangement.spacedBy(10.dp),
	) {
		if (isLastStep) {
			RegularButton(
				text = "저장",
				onClick = onSave,
				enabled = canProceed,
				modifier = Modifier.weight(1f),
			)
		} else if (!isStreetStep) {
			RegularButton(
				text = "다음",
				onClick = onNext,
				enabled = canProceed,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun RecordHandScreenPreview() {
	ThemePreview {
		RecordHandScreen(
			state = RecordHandState.Recording(tableId = "test"),
			onBack = {},
			onSelectHeroCard = {},
			onSelectBoardCard = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
			onNextStep = {},
			onPreviousStep = {},
			onUpdateResult = {},
			onUpdateMemo = {},
			onSave = {},
		)
	}
}

@ThemePreviews
@Composable
private fun RecordHandScreenTournamentPreview() {
	ThemePreview {
		RecordHandScreen(
			state = RecordHandState.Recording(
				tableId = "test",
				table = PokerTable(
					id = "test",
					date = LocalDate(2026, 3, 14),
					location = "WPT Korea",
					gameType = GameType.TOURNAMENT,
					startingStack = 10000.0,
					blinds = Blinds(sb = 50.0, bb = 100.0),
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				players = com.hand.log.record.model.RecordPlayers.create(
					playerCount = 9,
					defaultStack = 10000.0,
				),
				blinds = Blinds(sb = 50.0, bb = 100.0),
				heroHand = com.hand.log.domain.model.HeroHand(
					Card(Rank.ACE, Suit.SPADES),
					Card(Rank.KING, Suit.SPADES),
				),
			),
			onBack = {},
			onSelectHeroCard = {},
			onSelectBoardCard = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
			onUpdatePlayerStack = { _, _ -> },
			onConfirmAction = {},
			onRemoveLastAction = {},
			onNextStep = {},
			onPreviousStep = {},
			onUpdateResult = {},
			onUpdateMemo = {},
			onSave = {},
		)
	}
}
