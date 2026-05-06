package com.hand.log.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.utils.imeWindowInsets
import com.hand.log.utils.isKeyboardVisible
import com.hand.log.designsystem.component.HandySwitch
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
import com.hand.log.record.component.ShowdownStepContent
import com.hand.log.record.component.StreetStepContent
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.record.contract.displayName
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.domain.model.PocketCards
import com.hand.log.record.model.RecordPlayers.Companion.create
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@Composable
internal fun RecordHandScreen(
	state: RecordHandState.Recording,
	onBack: () -> Unit,
	onSelectHeroCard: () -> Unit,
	onSelectAllBoardCards: () -> Unit,
	onSelectBoardCard: (Street) -> Unit,
	onSelectSingleBoardCard: (Street, Int) -> Unit,
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
	onNavigateToStep: (RecordStep) -> Unit,
	onSelectShowdownCard: (Int) -> Unit,
	onUpdateMemo: (String) -> Unit,
	onToggleBbUnit: () -> Unit,
	onSave: () -> Unit,
	heroStackFocusRequester: FocusRequester = remember { FocusRequester() },
) {
	val colors = HandyTheme.colorScheme

	val keyboardVisible = isKeyboardVisible()

	BaseScaffold(
		applyNavigationBarsPadding = !keyboardVisible,
		containerColor = colors.background,
		topBar = {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				HandyTopAppbar(
					title = stringResource(Res.string.record_title),
					onBackEvent = {
						if (state.currentStep == RecordStep.SETUP) {
							onBack()
						} else {
							onPreviousStep()
						}
					},
					endContent = {
						HandySwitch(
							checked = state.useBbUnit,
							text = "BB",
							onCheckedChange = { onToggleBbUnit() },
						)
					},
				)
			}
		},
		bottomBar = {
			val isSetup = state.currentStep == RecordStep.SETUP
			val isShowdown = state.currentStep == RecordStep.SHOWDOWN
			if (isSetup || isShowdown) {
				BottomNavigationBar(
					currentStep = state.currentStep,
					canProceed = if (isSetup) state.canProceedFromSetup else true,
					onNext = onNextStep,
					onSave = onSave,
					modifier = Modifier.windowInsetsPadding(imeWindowInsets()),
				)
			}
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
		) {
			StepIndicator(
				currentStep = state.currentStep,
				activeSteps = state.activeSteps,
				onStepClick = onNavigateToStep,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
			)

			val scrollState = rememberScrollState()
			val needsAmount = state.currentActionType == ActionType.BET ||
				state.currentActionType == ActionType.RAISE

			// 키보드가 올라올 때 (레이즈/벳 입력 시) 자동으로 하단 스크롤
			LaunchedEffect(needsAmount) {
				if (needsAmount) {
					scrollState.animateScrollTo(scrollState.maxValue)
				}
			}

			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(scrollState)
					.windowInsetsPadding(imeWindowInsets())
					.padding(horizontal = 16.dp, vertical = 8.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				when (state.currentStep) {
					RecordStep.SETUP -> {
						SetupStepContent(
							state = state,
							onSelectHeroCard = onSelectHeroCard,
							onSelectBoardCards = onSelectAllBoardCards,
							onUpdateHeroStack = onUpdateHeroStack,
							onUpdateButtonSeat = onUpdateButtonSeat,
							onUpdateBlinds = onUpdateBlinds,
							heroStackFocusRequester = heroStackFocusRequester,
						)
					}

					RecordStep.PREFLOP, RecordStep.FLOP, RecordStep.TURN, RecordStep.RIVER -> {
						StreetStepContent(
							state = state,
							onSelectBoardCard = onSelectBoardCard,
							onSelectActionSeat = onSelectActionSeat,
							onSelectActionType = onSelectActionType,
							onUpdateActionAmount = onUpdateActionAmount,
							onUpdatePlayerStack = onUpdatePlayerStack,
							onConfirmAction = onConfirmAction,
							onRemoveLastAction = onRemoveLastAction,
							preflopPresets = state.actionPresets.preflopPresets,
							postflopPresets = state.actionPresets.postflopPresets,
						)
					}

					RecordStep.SHOWDOWN -> {
						ShowdownStepContent(
							state = state,
							onSelectSingleBoardCard = onSelectSingleBoardCard,
							onSelectHeroCard = onSelectHeroCard,
							onSelectShowdownCard = onSelectShowdownCard,
							onUpdateMemo = onUpdateMemo,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun StepIndicator(
	currentStep: RecordStep,
	activeSteps: List<RecordStep> = RecordStep.entries,
	onStepClick: ((RecordStep) -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.SpaceEvenly,
		verticalAlignment = Alignment.CenterVertically,
	) {
		activeSteps.forEachIndexed { index, step ->
			val isCurrent = step == currentStep
			val isPassed = step.ordinal < currentStep.ordinal
			val isClickable = onStepClick != null && isPassed && !isCurrent

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.weight(1f)
					.then(
						if (isClickable) {
							Modifier
								.clip(RoundedCornerShape(8.dp))
								.clickable { onStepClick(step) }
						} else {
							Modifier
						},
					),
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
					text = step.displayName(),
					style = HandyTheme.typography.regular12,
					color = if (isCurrent) colors.textPrimary else colors.textSecondary,
					textAlign = TextAlign.Center,
				)
			}
		}
	}
}

@Composable
private fun BottomNavigationBar(
	currentStep: RecordStep,
	canProceed: Boolean,
	onNext: () -> Unit,
	onSave: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	Row(
		modifier = modifier
			.fillMaxWidth()
			.background(colors.background)
			.padding(horizontal = 16.dp, vertical = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(10.dp),
	) {
		when (currentStep) {
			RecordStep.SHOWDOWN -> {
				RegularButton(
					text = stringResource(Res.string.btn_save),
					onClick = onSave,
					enabled = canProceed,
					modifier = Modifier.weight(1f),
				)
			}
			else -> {
				RegularButton(
					text = stringResource(Res.string.btn_next),
					onClick = onNext,
					enabled = canProceed,
					modifier = Modifier.weight(1f),
				)
			}
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
			onSelectAllBoardCards = {},
			onSelectBoardCard = {},
			onSelectSingleBoardCard = { _, _ -> },
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
			onNavigateToStep = {},
			onSelectShowdownCard = {},
			onUpdateMemo = {},
			onToggleBbUnit = {},
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
					gameType = GameType.Tournament(),
					heroSeat = 3,
					createdAt = 0L,
				),
				players = create(
					playerCount = 9,
					defaultStack = 10000.0,
				).update(3) {
					copy(cards = PocketCards(Card(Rank.ACE, Suit.SPADES), Card(Rank.KING, Suit.SPADES)))
				},
				blinds = Blinds(sb = 50.0, bb = 100.0),
			),
			onBack = {},
			onSelectHeroCard = {},
			onSelectAllBoardCards = {},
			onSelectBoardCard = {},
			onSelectSingleBoardCard = { _, _ -> },
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
			onNavigateToStep = {},
			onSelectShowdownCard = {},
			onUpdateMemo = {},
			onToggleBbUnit = {},
			onSave = {},
		)
	}
}
