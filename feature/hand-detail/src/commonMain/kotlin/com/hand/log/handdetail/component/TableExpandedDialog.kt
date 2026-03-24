package com.hand.log.handdetail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.HandRecord
import com.hand.log.ui.poker.CardSize
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.x
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TableExpandedDialog(
	hand: HandRecord,
	useBbUnit: Boolean,
	onDismiss: () -> Unit,
) {
	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		BoxWithConstraints(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			val containerWidth = maxWidth
			val containerHeight = maxHeight
			val density = LocalDensity.current

			val scaleX = with(density) { containerHeight.toPx() / containerWidth.toPx() }
			val scaleY = with(density) { containerWidth.toPx() / containerHeight.toPx() }

			Surface(
				modifier = Modifier
					.fillMaxSize()
					.graphicsLayer {
						rotationZ = 90f
						this.scaleX = scaleX
						this.scaleY = scaleY
					},
				color = HandyTheme.colorScheme.card,
			) {
				Box(modifier = Modifier.fillMaxSize()) {
					HandDetailTableView(
						hand = hand,
						useBbUnit = useBbUnit,
						seatCircleSize = 36.dp,
						seatCardSize = CardSize.SM,
						boardCardSize = CardSize.MD,
						modifier = Modifier
							.fillMaxSize()
							.align(Alignment.Center)
							.padding(12.dp),
					)

					Icon(
						painter = painterResource(Res.drawable.x),
						contentDescription = null,
						modifier = Modifier
							.align(Alignment.TopStart)
							.padding(16.dp)
							.size(32.dp)
							.clip(CircleShape)
							.clickable(onClick = onDismiss)
							.padding(4.dp),
						tint = HandyTheme.colorScheme.textSecondary,
					)
				}
			}
		}
	}
}
