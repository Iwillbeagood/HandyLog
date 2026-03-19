package com.hand.log.ui.poker

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ActionType

data class ActionColors(
	val background: Color,
	val content: Color,
)

@Composable
fun ActionType.actionColor(): ActionColors {
	val colors = HandyTheme.colorScheme
	val bg = when (this) {
		ActionType.FOLD -> colors.muted
		ActionType.CHECK -> colors.secondary
		ActionType.CALL -> colors.primary
		ActionType.BET -> colors.gold
		ActionType.RAISE -> colors.accent
		ActionType.ALL_IN -> colors.error
	}
	val content = when (this) {
		ActionType.FOLD -> colors.textSecondary
		ActionType.CHECK -> colors.onSecondary
		ActionType.BET, ActionType.RAISE -> colors.card
		else -> colors.onPrimary
	}
	return ActionColors(background = bg, content = content)
}

/** 액션 타입의 라벨/인디케이터 색상 (테이블뷰 등에서 사용) */
@Composable
fun ActionType.indicatorColor(): Color {
	val colors = HandyTheme.colorScheme
	return when (this) {
		ActionType.FOLD -> colors.textSecondary.copy(alpha = 0.4f)
		ActionType.CHECK -> colors.textPrimary
		ActionType.CALL -> colors.primary
		ActionType.BET -> colors.gold
		ActionType.RAISE -> colors.accent
		ActionType.ALL_IN -> colors.error
	}
}
