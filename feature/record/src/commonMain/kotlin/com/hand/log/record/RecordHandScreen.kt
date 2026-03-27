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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
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
import com.hand.log.record.contract.localizedLabel
import kotlinx.datetime.LocalDate
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import org.jetbrains.compose.resources.stringResource
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*

@Composable
internal fun RecordHandScreen(
	state: RecordHandState.Recording,
	onBack: () -> Unit,
	onSelectHeroCard: () -> Unit,
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
	onSelectShowdownCard: (Int) -> Unit,
	onUpdateMemo: (String) -> Unit,
	onShowTableEdit: () -> Unit,
	onToggleBbUnit: () -> Unit,
	onSave: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
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
			val isOpenerSelection = state.currentStep == RecordStep.PREFLOP &&
				state.streets.getActions(Street.PREFLOP).isEmpty() &&
				state.currentActionSeat == null
			val isStreetCompleted = !isSetup && !isShowdown && !isOpenerSelection &&
				state.currentActionSeat == null
			if (isSetup || isShowdown || isStreetCompleted) {
				BottomNavigationBar(
					currentStep = state.currentStep,
					canProceed = if (isSetup) state.canProceedFromSetup else true,
					onNext = onNextStep,
					onSave = onSave,
					modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
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
					.windowInsetsPadding(WindowInsets.ime)
					.padding(horizontal = 16.dp, vertical = 8.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				when (state.currentStep) {
					RecordStep.SETUP -> {
						SetupStepContent(
							state = state,
							onSelectHeroCard = onSelectHeroCard,
							onUpdateHeroStack = onUpdateHeroStack,
							onUpdateButtonSeat = onUpdateButtonSeat,
							onUpdateBlinds = onUpdateBlinds,
							onShowTableEdit = onShowTableEdit,
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
							preflopPresets = state.preflopPresets,
							postflopPresets = state.postflopPresets,
						)
					}

					RecordStep.SHOWDOWN -> {
						ShowdownStepContent(
							state = state,
							onSelectSingleBoardCard = onSelectSingleBoardCard,
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
					text = step.localizedLabel(),
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
			onSelectShowdownCard = {},
			onUpdateMemo = {},
			onShowTableEdit = {},
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
					playerCount = 9,
					heroSeat = 3,
					createdAt = 0L,
				),
				players = com.hand.log.record.model.RecordPlayers.create(
					playerCount = 9,
					defaultStack = 10000.0,
				),
				blinds = Blinds(sb = 50.0, bb = 100.0),
				heroHand = com.hand.log.domain.model.PocketCards(
					Card(Rank.ACE, Suit.SPADES),
					Card(Rank.KING, Suit.SPADES),
				),
			),
			onBack = {},
			onSelectHeroCard = {},
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
			onSelectShowdownCard = {},
			onUpdateMemo = {},
			onShowTableEdit = {},
			onToggleBbUnit = {},
			onSave = {},
		)
	}
}
