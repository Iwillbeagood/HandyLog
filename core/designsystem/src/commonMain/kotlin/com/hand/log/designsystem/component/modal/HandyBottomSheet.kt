package com.hand.log.designsystem.component.modal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme

/**
 * 앱 전체에서 사용하는 공용 바텀시트.
 *
 * - 타이틀 + 스크롤 가능한 콘텐츠 + 하단 버튼 영역
 * - `skipPartiallyExpanded = true`, `containerColor = card`
 *
 * @param title 시트 상단 타이틀
 * @param titleColor 타이틀 색상 (기본 textPrimary)
 * @param confirmText 확인/저장 버튼 텍스트 (null이면 버튼 미노출)
 * @param onConfirm 확인 버튼 클릭 콜백
 * @param confirmEnabled 확인 버튼 활성화 여부
 * @param subText 보조 버튼 텍스트 (null이면 미노출, 예: "초기화", "삭제")
 * @param onSub 보조 버튼 클릭 콜백
 * @param subContainerColor 보조 버튼 배경색 (기본 muted)
 * @param subContentColor 보조 버튼 텍스트색 (기본 textSecondary)
 * @param content 타이틀과 버튼 사이 메인 콘텐츠
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandyBottomSheet(
	onDismissRequest: () -> Unit,
	title: String,
	titleColor: Color = HandyTheme.colorScheme.textPrimary,
	modifier: Modifier = Modifier,
	confirmText: String? = null,
	onConfirm: () -> Unit = {},
	confirmEnabled: Boolean = true,
	subText: String? = null,
	onSub: () -> Unit = {},
	subContainerColor: Color = HandyTheme.colorScheme.muted,
	subContentColor: Color = HandyTheme.colorScheme.textSecondary,
	content: @Composable ColumnScope.() -> Unit,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val colors = HandyTheme.colorScheme

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		containerColor = colors.card,
		contentColor = colors.textPrimary,
		modifier = modifier,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.nestedScroll(SheetDragBlocker)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp)
				.padding(bottom = 32.dp),
		) {
			Text(
				text = title,
				style = HandyTheme.typography.bold20,
				color = titleColor,
			)

			VerticalSpacer(16.dp)

			content()

			val hasButtons = confirmText != null || subText != null
			if (hasButtons) {
				VerticalSpacer(20.dp)
				Row(
					modifier = Modifier.fillMaxWidth(),
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
		}
	}
}
