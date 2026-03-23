package com.hand.log.handdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.HandRecord
import com.hand.log.handdetail.component.ActionGridSection
import com.hand.log.handdetail.component.HandDetailTableView
import com.hand.log.handdetail.component.ResultSection

@Composable
internal fun HandDetailContent(
	hand: HandRecord,
	useBbUnit: Boolean,
	graphicsLayer: GraphicsLayer,
	modifier: Modifier = Modifier,
) {
	val colors = HandyTheme.colorScheme

	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.background(colors.background)
			.drawWithContent {
				graphicsLayer.record {
					this@drawWithContent.drawContent()
				}
				drawLayer(graphicsLayer)
			}
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		item {
			VerticalSpacer(8.dp)
			HandDetailTableView(
				hand = hand,
				useBbUnit = useBbUnit,
			)
		}

		item {
			ActionGridSection(
				hand = hand,
				useBbUnit = useBbUnit,
			)
		}

		item {
			ResultSection(hand = hand)
			VerticalSpacer(32.dp)
		}
	}
}
