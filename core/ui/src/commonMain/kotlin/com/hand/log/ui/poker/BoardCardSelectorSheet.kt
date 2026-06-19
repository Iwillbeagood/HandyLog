package com.hand.log.ui.poker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.modal.ModalButtonRow
import com.hand.log.designsystem.component.modal.SheetDragBlocker
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.Suit
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import handylog.core.res.generated.resources.btn_skip
import org.jetbrains.compose.resources.stringResource

/**
 * 보드 카드(Flop · Turn · River) 전용 선택 Sheet.
 * 상단에 보드 슬롯 프리뷰를 표시하고, [minCards] 이상 선택하면 완료할 수 있다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardCardSelectorSheet(
	title: String,
	selectedCards: Set<Card>,
	onCardsSelected: (List<Card>) -> Unit,
	onDismiss: () -> Unit,
	initialCards: List<Card> = emptyList(),
	minCards: Int = 3,
	maxCards: Int = 5,
	modifier: Modifier = Modifier,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme
	val pickedCards =
		remember(title) { mutableStateListOf<Card>().apply { addAll(initialCards) } }

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = colors.card,
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.nestedScroll(SheetDragBlocker)
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = title,
					style = HandyTheme.typography.bold18,
					color = colors.textPrimary,
				)
				Text(
					text = "${pickedCards.size} / $maxCards",
					style = HandyTheme.typography.medium14,
					color = if (pickedCards.size >= minCards) colors.primary else colors.textSecondary,
				)
			}

			BoardCardsPreview(
				pickedCards = pickedCards,
				onCardDeselected = { index -> pickedCards.removeAt(index) },
				modifier = Modifier.padding(bottom = 12.dp),
			)

			Suit.entries.forEach { suit ->
				SuitSection(
					suit = suit,
					disabledCards = selectedCards,
					pickedCards = pickedCards.toSet(),
					onCardSelected = { card ->
						if (pickedCards.size < maxCards) {
							pickedCards.add(card)
							if (pickedCards.size >= maxCards) {
								onCardsSelected(pickedCards.toList())
							}
						}
					},
					onCardDeselected = { card -> pickedCards.remove(card) },
				)
			}

			ModalButtonRow(
				confirmText = stringResource(Res.string.btn_complete),
				onConfirm = { onCardsSelected(pickedCards.toList()) },
				confirmEnabled = pickedCards.size >= minCards,
				subText = stringResource(Res.string.btn_skip),
				onSub = onDismiss,
				modifier = Modifier.padding(top = 8.dp),
			)
		}
	}
}
