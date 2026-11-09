package com.hand.log.record.quick.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.HandySelector
import com.hand.log.designsystem.component.modal.HandyBottomSheet
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.record.quick.contract.QuickRecordModalEffect
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quick_record_finish_hero_position
import handylog.core.res.generated.resources.quick_record_hero_select
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickHeroSheet(
	hero: QuickRecordModalEffect.ShowHeroSelect,
	onHeroSelect: (String) -> Unit,
	onDismiss: () -> Unit,
) {
	HandyBottomSheet(
		onDismissRequest = onDismiss,
		title = stringResource(Res.string.quick_record_hero_select),
	) {
		QuickHeroContent(hero = hero, onHeroSelect = onHeroSelect)
	}
}

@Composable
private fun QuickHeroContent(
	hero: QuickRecordModalEffect.ShowHeroSelect,
	onHeroSelect: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(6.dp),
	) {
		FinishLabel(text = stringResource(Res.string.quick_record_finish_hero_position))
		HandySelector(
			options = hero.positionOptions,
			selected = hero.heroPosition.orEmpty(),
			onSelect = onHeroSelect,
			selectedColor = HandyTheme.colorScheme.gold,
		)
	}
}

@ThemePreviews
@Composable
private fun QuickHeroContentPreview() {
	ThemePreview {
		QuickHeroContent(
			hero = QuickRecordModalEffect.ShowHeroSelect(
				positionOptions = listOf("UTG", "MP", "CO", "BTN", "SB", "BB"),
				heroPosition = "BTN",
			),
			onHeroSelect = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun QuickHeroContentUnsetPreview() {
	ThemePreview {
		QuickHeroContent(
			hero = QuickRecordModalEffect.ShowHeroSelect(
				positionOptions = listOf("UTG", "MP", "CO", "BTN", "SB", "BB"),
				heroPosition = null,
			),
			onHeroSelect = {},
			modifier = Modifier.padding(16.dp),
		)
	}
}
