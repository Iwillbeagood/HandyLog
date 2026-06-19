package com.hand.log.designsystem.component.modal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme

@Composable
fun ModalButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	isNegative: Boolean = false,
) {
	if (isNegative) {
		RegularButton(
			text = text,
			onClick = onClick,
			containerColor = HandyTheme.colorScheme.secondary,
			contentColor = HandyTheme.colorScheme.onSecondary,
			textStyle = HandyTheme.typography.bold16,
			modifier = modifier,
		)
	} else {
		RegularButton(
			text = text,
			onClick = onClick,
			enabled = enabled,
			textStyle = HandyTheme.typography.bold16,
			modifier = modifier,
		)
	}
}

/**
 * 바텀시트 하단 버튼 영역 공용 컴포넌트.
 *
 * - 확인 버튼([confirmText])과 보조 버튼([subText])을 가로로 균등 배치한다.
 * - 둘 중 하나만 지정하면 단일 버튼으로, 둘 다 null이면 아무것도 그리지 않는다.
 *
 * @param confirmText 확인 버튼 텍스트 (null이면 미노출)
 * @param onConfirm 확인 버튼 클릭 콜백
 * @param confirmEnabled 확인 버튼 활성화 여부
 * @param subText 보조 버튼 텍스트 (null이면 미노출, 예: "초기화", "삭제")
 * @param onSub 보조 버튼 클릭 콜백
 * @param subContainerColor 보조 버튼 배경색 (기본 muted)
 * @param subContentColor 보조 버튼 텍스트색 (기본 textSecondary)
 */
@Composable
fun ModalButtonRow(
	modifier: Modifier = Modifier,
	confirmText: String? = null,
	onConfirm: () -> Unit = {},
	confirmEnabled: Boolean = true,
	subText: String? = null,
	onSub: () -> Unit = {},
	subContainerColor: Color = HandyTheme.colorScheme.muted,
	subContentColor: Color = HandyTheme.colorScheme.textSecondary,
) {
	if (confirmText == null && subText == null) return

	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		if (subText != null) {
			RegularButton(
				text = subText,
				onClick = onSub,
				containerColor = subContainerColor,
				contentColor = subContentColor,
				modifier = Modifier.weight(1f),
			)
		}
		if (confirmText != null) {
			RegularButton(
				text = confirmText,
				onClick = onConfirm,
				enabled = confirmEnabled,
				modifier = Modifier.weight(1f),
			)
		}
	}
}

@ThemePreviews
@Composable
private fun ModalButtonRowPreview() {
	ThemePreview {
		ModalButtonRow(
			confirmText = "저장",
			subText = "삭제",
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun ModalButtonRowConfirmOnlyPreview() {
	ThemePreview {
		ModalButtonRow(
			confirmText = "완료",
			modifier = Modifier.padding(16.dp),
		)
	}
}
