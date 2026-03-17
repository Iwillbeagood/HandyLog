package com.hand.log.designsystem.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.VerticalSpacer
import com.hand.log.designsystem.theme.HandyTheme
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.btn_complete
import org.jetbrains.compose.resources.stringResource
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews

object BottomSheet {
	@OptIn(ExperimentalMaterial3Api::class)
	val sheetState = SheetState(true, { 1f }, { 1f }, SheetValue.Expanded, { true }, false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
	onDismissRequest: () -> Unit,
	sheetState: SheetState = rememberModalBottomSheetState(true),
	sheetTitle: String = "",
	sheetTitleColor: Color = HandyTheme.colorScheme.textPrimary,
	sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
	contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
	contentTopPadding: Dp = 16.dp,
	sheetContent: @Composable () -> Unit = {},
	sheetButton: (@Composable ColumnScope.() -> Unit)? = null,
) {
	ModalBottomSheet(
		sheetState = sheetState,
		sheetMaxWidth = sheetMaxWidth,
		onDismissRequest = onDismissRequest,
		containerColor = HandyTheme.colorScheme.modalBackground,
		shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
		dragHandle = null,
	) {

		Column(
			modifier = Modifier
				.background(HandyTheme.colorScheme.modalBackground)
				.padding(
					top = 18.dp,
					bottom = 24.dp,
				)
				.navigationBarsPadding(),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
			) {
				if (sheetTitle.isNotEmpty()) {
					Text(
						text = sheetTitle,
						style = HandyTheme.typography.bold16,
						color = sheetTitleColor,
					)
				}
			}
			VerticalSpacer(contentTopPadding)
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(contentPadding),
			) {
				sheetContent()
			}
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
			) {
				sheetButton?.let { button2 ->
					VerticalSpacer(24.dp)
					button2()
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultBottomSheet(
	onDismissRequest: () -> Unit,
	sheetTitle: String = "",
	confirmText: String = stringResource(Res.string.btn_complete),
	onConfirmClick: () -> Unit = {},
	confirmEnabled: Boolean = true,
	sheetState: SheetState = rememberModalBottomSheetState(true),
	sheetContent: @Composable () -> Unit,
) {
	BaseBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		sheetTitle = sheetTitle,
		sheetContent = sheetContent,
		sheetButton = {
			ModalButton(
				text = confirmText,
				onClick = {
					onDismissRequest()
					onConfirmClick()
				},
				enabled = confirmEnabled,
				modifier = Modifier.fillMaxWidth(),
			)
		},
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun HmBottomScaffoldPreview() {
	ThemePreview {
		DefaultBottomSheet(
			sheetState = BottomSheet.sheetState,
			sheetTitle = "화물등록",
			sheetContent = {
				Text(
					text = "화물 등록을 하시겠습니까?",
					style = HandyTheme.typography.medium16,
					color = HandyTheme.colorScheme.textPrimary,
				)
			},
			onDismissRequest = {},
		)
	}
}
