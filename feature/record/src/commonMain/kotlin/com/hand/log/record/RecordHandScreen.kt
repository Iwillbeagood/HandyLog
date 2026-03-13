package com.hand.log.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.GameType
import com.hand.log.domain.model.Street
import com.hand.log.record.contract.CardSelectorTarget
import com.hand.log.record.contract.RecordHandState
import com.hand.log.record.contract.RecordStep
import com.hand.log.ui.poker.CardSelectorSheet
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordHandScreen(
	state: RecordHandState.Recording,
	onBack: () -> Unit,
	onSelectHeroCard: (Int) -> Unit,
	onSelectBoardCard: (Street, Int) -> Unit,
	onCardSelected: (Card) -> Unit,
	onCloseCardSelector: () -> Unit,
	onUpdateHeroStack: (String) -> Unit,
	onUpdateButtonSeat: (Int) -> Unit,
	onUpdateBlinds: (String, String) -> Unit,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	onNextStep: () -> Unit,
	onPreviousStep: () -> Unit,
	onUpdateResult: (String) -> Unit,
	onUpdateMemo: (String) -> Unit,
	onSave: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Scaffold(
		containerColor = colors.background,
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = "핸드 기록",
						style = HandyTheme.typography.bold18,
						color = colors.textPrimary,
					)
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "뒤로",
							tint = colors.textPrimary,
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = colors.background,
				),
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
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
		) {
			// Step indicator
			StepIndicator(
				currentStep = state.currentStep,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
			)

			// Step content
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

		// Card Selector Bottom Sheet
		if (state.showCardSelector && state.cardSelectorTarget != null) {
			val title = when (state.cardSelectorTarget) {
				is CardSelectorTarget.HeroCard -> "히어로 카드 ${state.cardSelectorTarget.index + 1}"
				is CardSelectorTarget.BoardCard -> "${state.cardSelectorTarget.street.label} 카드 ${state.cardSelectorTarget.index + 1}"
			}
			CardSelectorSheet(
				title = title,
				selectedCards = state.selectedCards,
				onCardSelected = onCardSelected,
				onDismiss = onCloseCardSelector,
			)
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
						style = HandyTheme.typography.bold14,
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

@Composable
private fun SetupStepContent(
	state: RecordHandState.Recording,
	onSelectHeroCard: (Int) -> Unit,
	onUpdateHeroStack: (String) -> Unit,
	onUpdateButtonSeat: (Int) -> Unit,
	onUpdateBlinds: (String, String) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
		// Hero Cards
		SectionTitle("히어로 카드")
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			// Card 1
			PlayingCard(
				card = state.heroCards.getOrNull(0),
				size = CardSize.LG,
				onClick = { onSelectHeroCard(0) },
			)
			// Card 2
			PlayingCard(
				card = state.heroCards.getOrNull(1),
				size = CardSize.LG,
				onClick = { onSelectHeroCard(1) },
			)
		}

		// Hero Stack
		SectionTitle("히어로 스택")
		OutlinedTextField(
			value = if (state.heroStack == 0.0) "" else state.heroStack.toLong().toString(),
			onValueChange = onUpdateHeroStack,
			placeholder = { Text("스택 입력", color = colors.textSecondary) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			colors = OutlinedTextFieldDefaults.colors(
				focusedBorderColor = colors.primary,
				unfocusedBorderColor = colors.inputBorder,
				cursorColor = colors.primary,
				focusedTextColor = colors.textPrimary,
				unfocusedTextColor = colors.textPrimary,
			),
		)

		// Button Seat
		SectionTitle("버튼 좌석")
		ButtonSeatSelector(
			selectedSeat = state.buttonSeat,
			playerCount = state.table?.playerCount ?: 9,
			onSeatSelected = onUpdateButtonSeat,
		)

		// Blinds (only for tournament)
		if (state.table?.gameType == GameType.TOURNAMENT) {
			SectionTitle("블라인드")
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				OutlinedTextField(
					value = state.blinds?.sb?.toLong()?.toString() ?: "",
					onValueChange = { sb ->
						onUpdateBlinds(sb, state.blinds?.bb?.toLong()?.toString() ?: "")
					},
					placeholder = { Text("SB", color = colors.textSecondary) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.weight(1f),
					singleLine = true,
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = colors.primary,
						unfocusedBorderColor = colors.inputBorder,
						cursorColor = colors.primary,
						focusedTextColor = colors.textPrimary,
						unfocusedTextColor = colors.textPrimary,
					),
				)
				OutlinedTextField(
					value = state.blinds?.bb?.toLong()?.toString() ?: "",
					onValueChange = { bb ->
						onUpdateBlinds(state.blinds?.sb?.toLong()?.toString() ?: "", bb)
					},
					placeholder = { Text("BB", color = colors.textSecondary) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.weight(1f),
					singleLine = true,
					colors = OutlinedTextFieldDefaults.colors(
						focusedBorderColor = colors.primary,
						unfocusedBorderColor = colors.inputBorder,
						cursorColor = colors.primary,
						focusedTextColor = colors.textPrimary,
						unfocusedTextColor = colors.textPrimary,
					),
				)
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonSeatSelector(
	selectedSeat: Int,
	playerCount: Int,
	onSeatSelected: (Int) -> Unit,
) {
	val colors = HandyTheme.colorScheme

	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		(1..playerCount).forEach { seat ->
			val isSelected = seat == selectedSeat
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(CircleShape)
					.background(if (isSelected) colors.primary else colors.muted)
					.clickable { onSeatSelected(seat) },
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "$seat",
					color = if (isSelected) colors.onPrimary else colors.textSecondary,
					style = HandyTheme.typography.bold14,
				)
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StreetStepContent(
	state: RecordHandState.Recording,
	onSelectBoardCard: (Street, Int) -> Unit,
	onSelectActionSeat: (Int) -> Unit,
	onSelectActionType: (ActionType) -> Unit,
	onUpdateActionAmount: (String) -> Unit,
	onConfirmAction: () -> Unit,
	onRemoveLastAction: () -> Unit,
	onUpdateResult: (String) -> Unit,
	onUpdateMemo: (String) -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val currentStreet = state.currentStreet
	val streetData = state.streets[currentStreet]

	Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
		// Board Cards (not for PREFLOP)
		if (currentStreet != Street.PREFLOP) {
			SectionTitle("보드 카드")
			val cardCount = when (currentStreet) {
				Street.FLOP -> 3
				Street.TURN -> 1
				Street.RIVER -> 1
				else -> 0
			}
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				// Show previously dealt cards from earlier streets
				if (currentStreet == Street.TURN || currentStreet == Street.RIVER) {
					val flopCards = state.streets[Street.FLOP]?.cards ?: emptyList()
					flopCards.forEach { card ->
						PlayingCard(
							card = card,
							size = CardSize.MD,
						)
					}
				}
				if (currentStreet == Street.RIVER) {
					val turnCards = state.streets[Street.TURN]?.cards ?: emptyList()
					turnCards.forEach { card ->
						PlayingCard(
							card = card,
							size = CardSize.MD,
						)
					}
				}
				// Current street cards
				(0 until cardCount).forEach { index ->
					val card = streetData?.cards?.getOrNull(index)
					PlayingCard(
						card = card,
						size = CardSize.LG,
						onClick = { onSelectBoardCard(currentStreet, index) },
					)
				}
			}
		}

		// Current Pot
		SectionTitle("현재 팟")
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(8.dp))
				.background(colors.card)
				.padding(12.dp),
		) {
			Text(
				text = "${state.currentPot.toLong()}",
				style = HandyTheme.typography.bold18,
				color = colors.gold,
			)
		}

		// Action History
		val actions = streetData?.actions ?: emptyList()
		if (actions.isNotEmpty()) {
			SectionTitle("액션 기록")
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(8.dp))
					.background(colors.card)
					.padding(12.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp),
			) {
				actions.forEach { action ->
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(
							text = "좌석 ${action.playerSeat}: ${action.type.label}",
							style = HandyTheme.typography.regular14,
							color = colors.textPrimary,
						)
						action.amount?.let {
							Text(
								text = "${it.toLong()}",
								style = HandyTheme.typography.medium14,
								color = colors.gold,
							)
						}
					}
				}

				// Undo button
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End,
				) {
					IconButton(onClick = onRemoveLastAction, modifier = Modifier.size(32.dp)) {
						Icon(
							Icons.Default.Delete,
							contentDescription = "되돌리기",
							tint = colors.error,
							modifier = Modifier.size(18.dp),
						)
					}
				}
			}
		}

		// Action Input Area
		SectionTitle("액션 입력")

		// Player seat buttons
		Text(
			text = "좌석 선택",
			style = HandyTheme.typography.medium14,
			color = colors.textSecondary,
		)
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			val playerCount = state.table?.playerCount ?: 9
			(1..playerCount).forEach { seat ->
				val isSelected = seat == state.currentActionSeat
				Box(
					modifier = Modifier
						.size(40.dp)
						.clip(CircleShape)
						.background(if (isSelected) colors.primary else colors.muted)
						.clickable { onSelectActionSeat(seat) },
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = "$seat",
						color = if (isSelected) colors.onPrimary else colors.textSecondary,
						style = HandyTheme.typography.bold14,
					)
				}
			}
		}

		// Action type buttons
		Text(
			text = "액션 선택",
			style = HandyTheme.typography.medium14,
			color = colors.textSecondary,
		)
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			ActionType.entries.forEach { actionType ->
				val isSelected = actionType == state.currentActionType
				val buttonColor = when (actionType) {
					ActionType.FOLD -> colors.muted
					ActionType.CHECK -> colors.secondary
					ActionType.CALL -> colors.primary
					ActionType.BET -> colors.gold
					ActionType.RAISE -> colors.accent
					ActionType.ALL_IN -> colors.error
				}
				val contentColor = when {
					isSelected -> when (actionType) {
						ActionType.FOLD -> colors.textPrimary
						ActionType.CHECK -> colors.onSecondary
						ActionType.BET, ActionType.RAISE -> colors.card
						else -> colors.onPrimary
					}
					else -> colors.textSecondary
				}

				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(if (isSelected) buttonColor else colors.muted)
						.clickable { onSelectActionType(actionType) }
						.padding(horizontal = 16.dp, vertical = 10.dp),
				) {
					Text(
						text = actionType.label,
						style = HandyTheme.typography.bold14,
						color = if (isSelected) contentColor else colors.textSecondary,
					)
				}
			}
		}

		// Amount input for BET/RAISE
		if (state.currentActionType == ActionType.BET ||
			state.currentActionType == ActionType.RAISE ||
			state.currentActionType == ActionType.ALL_IN
		) {
			Text(
				text = "금액",
				style = HandyTheme.typography.medium14,
				color = colors.textSecondary,
			)
			OutlinedTextField(
				value = state.currentActionAmount,
				onValueChange = onUpdateActionAmount,
				placeholder = { Text("금액 입력", color = colors.textSecondary) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)

			// Pot % quick buttons
			if (state.currentPot > 0) {
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.horizontalScroll(rememberScrollState()),
				) {
					listOf(33, 50, 75, 100).forEach { percent ->
						val amount = (state.currentPot * percent / 100).toLong().toString()
						OutlinedButton(
							onClick = { onUpdateActionAmount(amount) },
							shape = RoundedCornerShape(8.dp),
						) {
							Text(
								text = "$percent%",
								style = HandyTheme.typography.medium14,
							)
						}
					}
				}
			}
		}

		// Confirm action button
		if (state.currentActionSeat != null && state.currentActionType != null) {
			Button(
				onClick = onConfirmAction,
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(
					containerColor = colors.primary,
					contentColor = colors.onPrimary,
				),
				shape = RoundedCornerShape(12.dp),
			) {
				Text(
					text = "액션 추가",
					style = HandyTheme.typography.bold14,
				)
			}
		}

		// Result & Memo (show on all street steps for convenience)
		if (state.currentStep == RecordStep.RIVER) {
			Spacer(modifier = Modifier.height(8.dp))
			SectionTitle("결과 (수익/손실)")
			OutlinedTextField(
				value = state.result,
				onValueChange = onUpdateResult,
				placeholder = { Text("결과 금액 (+/-)", color = colors.textSecondary) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)

			SectionTitle("메모")
			OutlinedTextField(
				value = state.memo,
				onValueChange = onUpdateMemo,
				placeholder = { Text("메모 입력", color = colors.textSecondary) },
				modifier = Modifier.fillMaxWidth(),
				minLines = 3,
				colors = OutlinedTextFieldDefaults.colors(
					focusedBorderColor = colors.primary,
					unfocusedBorderColor = colors.inputBorder,
					cursorColor = colors.primary,
					focusedTextColor = colors.textPrimary,
					unfocusedTextColor = colors.textPrimary,
				),
			)
		}

		Spacer(modifier = Modifier.height(16.dp))
	}
}

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

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(colors.background)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		// Back button
		if (!isFirstStep) {
			OutlinedButton(
				onClick = onPrevious,
				modifier = Modifier
					.weight(1f)
					.height(48.dp),
				shape = RoundedCornerShape(12.dp),
			) {
				Icon(
					Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "이전",
					style = HandyTheme.typography.bold14,
				)
			}
		}

		// Next / Save button
		Button(
			onClick = if (isLastStep) onSave else onNext,
			modifier = Modifier
				.weight(1f)
				.height(48.dp),
			enabled = canProceed,
			colors = ButtonDefaults.buttonColors(
				containerColor = if (isLastStep) colors.primary else colors.primary,
				contentColor = colors.onPrimary,
			),
			shape = RoundedCornerShape(12.dp),
		) {
			if (isLastStep) {
				Icon(
					Icons.Default.Check,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "저장",
					style = HandyTheme.typography.bold14,
				)
			} else {
				Text(
					text = "다음",
					style = HandyTheme.typography.bold14,
				)
				Spacer(modifier = Modifier.width(4.dp))
				Icon(
					Icons.AutoMirrored.Filled.ArrowForward,
					contentDescription = null,
					modifier = Modifier.size(18.dp),
				)
			}
		}
	}
}

@Composable
private fun SectionTitle(title: String) {
	val colors = HandyTheme.colorScheme
	Text(
		text = title,
		style = HandyTheme.typography.bold16,
		color = colors.textPrimary,
		modifier = Modifier.padding(bottom = 8.dp),
	)
}

@Preview
@Composable
private fun RecordHandScreenPreview() {
	HandLogTheme {
		RecordHandScreen(
			state = RecordHandState.Recording(tableId = "test"),
			onBack = {},
			onSelectHeroCard = {},
			onSelectBoardCard = { _, _ -> },
			onCardSelected = {},
			onCloseCardSelector = {},
			onUpdateHeroStack = {},
			onUpdateButtonSeat = {},
			onUpdateBlinds = { _, _ -> },
			onSelectActionSeat = {},
			onSelectActionType = {},
			onUpdateActionAmount = {},
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
