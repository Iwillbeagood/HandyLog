package com.hand.log.settings.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandySegmentedTab
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.FeedbackCategory
import com.hand.log.settings.contact.contract.ContactState
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.contact_attach_add
import handylog.core.res.generated.resources.contact_attach_label
import handylog.core.res.generated.resources.contact_field_content_hint
import handylog.core.res.generated.resources.contact_field_content_label
import handylog.core.res.generated.resources.contact_field_email_hint
import handylog.core.res.generated.resources.contact_field_email_label
import handylog.core.res.generated.resources.contact_field_title_hint
import handylog.core.res.generated.resources.contact_field_title_label
import handylog.core.res.generated.resources.contact_intro
import handylog.core.res.generated.resources.contact_submit
import handylog.core.res.generated.resources.contact_tab_error
import handylog.core.res.generated.resources.contact_tab_feature
import handylog.core.res.generated.resources.contact_title
import handylog.core.res.generated.resources.image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactScreen(
	state: ContactState,
	onCategorySelect: (FeedbackCategory) -> Unit,
	onTitleChange: (String) -> Unit,
	onContentChange: (String) -> Unit,
	onEmailChange: (String) -> Unit,
	onSubmit: () -> Unit,
	onBack: () -> Unit,
) {
	val colors = HandyTheme.colorScheme

	BaseScaffold(
		topBar = {
			HandyTopAppbar(
				title = stringResource(Res.string.contact_title),
				onBackEvent = onBack,
			)
		},
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp),
		) {
			Text(
				text = stringResource(Res.string.contact_intro),
				style = HandyTheme.typography.regular14,
				color = colors.textSecondary,
			)

			HandySegmentedTab(
				options = CATEGORIES,
				selected = state.category,
				onSelect = onCategorySelect,
				label = { category -> stringResource(category.labelRes()) },
			)

			HandyTextField(
				value = state.title,
				onValueChange = onTitleChange,
				label = stringResource(Res.string.contact_field_title_label),
				placeholder = stringResource(Res.string.contact_field_title_hint),
			)

			HandyTextField(
				value = state.content,
				onValueChange = onContentChange,
				label = stringResource(Res.string.contact_field_content_label),
				placeholder = stringResource(Res.string.contact_field_content_hint),
				minLines = 6,
			)

			HandyTextField(
				value = state.email,
				onValueChange = onEmailChange,
				label = stringResource(Res.string.contact_field_email_label),
				placeholder = stringResource(Res.string.contact_field_email_hint),
				keyboardType = KeyboardType.Email,
			)

			AttachmentBox()

			RegularButton(
				text = stringResource(Res.string.contact_submit),
				onClick = onSubmit,
				enabled = state.canSubmit,
			)
		}
	}
}

private val CATEGORIES = listOf(FeedbackCategory.FEATURE, FeedbackCategory.ERROR)

private fun FeedbackCategory.labelRes() = when (this) {
	FeedbackCategory.FEATURE -> Res.string.contact_tab_feature
	FeedbackCategory.ERROR -> Res.string.contact_tab_error
}

/**
 * 스크린샷 첨부 영역. 현재는 디자인 노출용 정적 플레이스홀더이며 실제 첨부는 추후 연동한다.
 */
@Composable
private fun AttachmentBox() {
	val colors = HandyTheme.colorScheme

	HandySectionLabel(stringResource(Res.string.contact_attach_label)) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.height(88.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(colors.muted)
				.border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp)),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.image),
				contentDescription = null,
				tint = colors.textSecondary,
				modifier = Modifier.size(24.dp),
			)
			Spacer(modifier = Modifier.height(6.dp))
			Text(
				text = stringResource(Res.string.contact_attach_add),
				style = HandyTheme.typography.regular14,
				color = colors.textSecondary,
			)
		}
	}
}

@ThemePreviews
@Composable
private fun ContactScreenPreview() {
	ThemePreview {
		ContactScreen(
			state = ContactState(
				category = FeedbackCategory.FEATURE,
				title = "통계 화면에 필터를 추가해주세요",
				content = "기간별로 필터링할 수 있으면 좋겠어요.",
				email = "user@example.com",
			),
			onCategorySelect = {},
			onTitleChange = {},
			onContentChange = {},
			onEmailChange = {},
			onSubmit = {},
			onBack = {},
		)
	}
}
