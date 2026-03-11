package com.hand.log.designsystem.etc

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role

fun Modifier.circleLayout() =
	layout { measurable, constraints ->
		val placeable = measurable.measure(constraints)
		val currentHeight = placeable.height
		val currentWidth = placeable.width
		val newDiameter = maxOf(currentHeight, currentWidth)

		layout(newDiameter, newDiameter) {
			placeable.placeRelative(
				(newDiameter - currentWidth) / 2,
				(newDiameter - currentHeight) / 2,
			)
		}
	}

fun Modifier.clickableSingle(
	onClick: () -> Unit,
	enabled: Boolean = true,
	onClickLabel: String? = null,
	role: Role? = null,
) = composed(
	inspectorInfo = debugInspectorInfo {
		name = "clickable"
		properties["enabled"] = enabled
		properties["onClickLabel"] = onClickLabel
		properties["role"] = role
		properties["onClick"] = onClick
	},
) {
	val multipleEventsCutter = remember { MultipleEventsCutter.get() }
	Modifier.clickable(
		enabled = enabled,
		onClickLabel = onClickLabel,
		onClick = { multipleEventsCutter.processEvent(onClick) },
		role = role,
		indication = LocalIndication.current,
		interactionSource = remember { MutableInteractionSource() },
	)
}
