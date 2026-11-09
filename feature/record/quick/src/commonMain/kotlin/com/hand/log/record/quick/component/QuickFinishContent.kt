package com.hand.log.record.quick.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.quick.CardOwner
import com.hand.log.record.quick.contract.QuickFinishForm
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.board_cards
import handylog.core.res.generated.resources.quick_record_finish_desc
import handylog.core.res.generated.resources.quick_record_finish_hero_position
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickFinishContent(
	form: QuickFinishForm,
	onHeroPositionSelect: (String) -> Unit,
	onCardsClick: (CardOwner) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		Text(
			text = stringResource(Res.string.quick_record_finish_desc),
			style = HandyTheme.typography.medium12,
			color = HandyTheme.colorScheme.textSecondary,
		)
		if (form.heroPositionOptions.isNotEmpty()) {
			Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
				FinishLabel(text = stringResource(Res.string.quick_record_finish_hero_position))
				HandySelector(
					options = form.heroPositionOptions,
					selected = form.heroPosition?.label.orEmpty(),
					onSelect = onHeroPositionSelect,
					selectedColor = HandyTheme.colorScheme.gold,
				)
			}
		}
		if (form.showCards) {
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				form.heroPosition?.let { position ->
					CardGroup(
						cards = form.heroCards,
						slots = HOLE_CARD_SLOTS,
						onClick = { onCardsClick(CardOwner.Hero) },
					) {
						HeroLabel(position = position.label)
					}
				}
				form.showdown.forEach { hand ->
					CardGroup(
						cards = hand.cards,
						slots = HOLE_CARD_SLOTS,
						onClick = { onCardsClick(CardOwner.Opponent(hand.position)) },
					) {
						FinishLabel(text = hand.position.label)
					}
				}
				if (form.showBoard) {
					CardGroup(
						cards = form.board,
						slots = form.board.size,
						onClick = { onCardsClick(CardOwner.Board) },
					) {
						FinishLabel(text = stringResource(Res.string.board_cards))
					}
				}
			}
		}
	}
}

@ThemePreviews
@Composable
private fun QuickFinishContentShowdownPreview() {
	ThemePreview {
		QuickRecordPreviewData.state(QuickRecordPreviewData.reviewInput).finishForm?.let { form ->
			QuickFinishContent(
				form = form,
				onHeroPositionSelect = {},
				onCardsClick = {},
				modifier = Modifier.padding(16.dp),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun QuickFinishContentHeroMissingPreview() {
	ThemePreview {
		QuickRecordPreviewData.state(QuickRecordPreviewData.heroMissingInput).finishForm?.let { form ->
			QuickFinishContent(
				form = form,
				onHeroPositionSelect = {},
				onCardsClick = {},
				modifier = Modifier.padding(16.dp),
			)
		}
	}
}
