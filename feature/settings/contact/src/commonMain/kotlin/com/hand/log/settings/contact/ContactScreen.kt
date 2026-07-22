package com.hand.log.settings.contact

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.component.BaseScaffold
import com.hand.log.designsystem.component.HandySectionLabel
import com.hand.log.designsystem.component.HandyTextField
import com.hand.log.designsystem.component.HandyTopAppbar
import com.hand.log.designsystem.component.RegularButton
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.settings.contact.contract.ContactState
import com.hand.log.platform.image.PickedImage
import com.hand.log.platform.image.rememberImagePicker
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.contact_attach_label
import handylog.core.res.generated.resources.contact_field_content_hint
import handylog.core.res.generated.resources.contact_field_content_label
import handylog.core.res.generated.resources.contact_field_email_hint
import handylog.core.res.generated.resources.contact_field_email_label
import handylog.core.res.generated.resources.contact_field_title_hint
import handylog.core.res.generated.resources.contact_field_title_label
import handylog.core.res.generated.resources.contact_intro
import handylog.core.res.generated.resources.contact_submit
import handylog.core.res.generated.resources.contact_title
import handylog.core.res.generated.resources.plus
import handylog.core.res.generated.resources.x
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactScreen(
	state: ContactState,
	onTitleChange: (String) -> Unit,
	onContentChange: (String) -> Unit,
	onEmailChange: (String) -> Unit,
	onImagePicked: (PickedImage) -> Unit,
	onImageRemove: (Int) -> Unit,
	onSubmit: () -> Unit,
	onBack: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val imagePicker = rememberImagePicker(onPicked = onImagePicked)

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

			AttachmentBox(
				images = state.images,
				canAddImage = state.canAddImage,
				onAddClick = imagePicker::launch,
				onImageRemove = onImageRemove,
			)

			RegularButton(
				text = stringResource(Res.string.contact_submit),
				onClick = onSubmit,
				enabled = state.canSubmit,
				loading = state.isSubmitting,
			)
		}
	}
}

/**
 * 스크린샷 첨부 영역. 선택한 이미지를 정사각 썸네일로 나열하고, 최대 개수 미만이면 추가 버튼을 노출한다.
 */
@Composable
private fun AttachmentBox(
	images: List<PickedImage>,
	canAddImage: Boolean,
	onAddClick: () -> Unit,
	onImageRemove: (Int) -> Unit,
) {
	HandySectionLabel(stringResource(Res.string.contact_attach_label)) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(88.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			images.forEachIndexed { index, image ->
				AttachmentThumbnail(
					image = image,
					onRemove = { onImageRemove(index) },
				)
			}
			if (canAddImage) {
				AddAttachmentButton(onClick = onAddClick)
			}
		}
	}
}

@Composable
private fun AttachmentThumbnail(
	image: PickedImage,
	onRemove: () -> Unit,
) {
	val colors = HandyTheme.colorScheme
	val bitmap = remember(image) { runCatching { image.bytes.decodeToImageBitmap() }.getOrNull() }

	Box(
		modifier = Modifier
			.fillMaxHeight()
			.aspectRatio(1f)
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted)
			.border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp)),
	) {
		if (bitmap != null) {
			Image(
				bitmap = bitmap,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}
		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(4.dp)
				.size(20.dp)
				.clip(CircleShape)
				.background(colors.error)
				.clickable(onClick = onRemove),
			contentAlignment = Alignment.Center,
		) {
			Icon(
				painter = painterResource(Res.drawable.x),
				contentDescription = null,
				tint = Color.White,
				modifier = Modifier.size(12.dp),
			)
		}
	}
}

@Composable
private fun AddAttachmentButton(onClick: () -> Unit) {
	val colors = HandyTheme.colorScheme

	Box(
		modifier = Modifier
			.fillMaxHeight()
			.aspectRatio(1f)
			.clip(RoundedCornerShape(8.dp))
			.background(colors.muted)
			.border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
			.clickable(onClick = onClick),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			painter = painterResource(Res.drawable.plus),
			contentDescription = null,
			tint = colors.textSecondary,
			modifier = Modifier.size(24.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun ContactScreenPreview() {
	ThemePreview {
		ContactScreen(
			state = ContactState(
				title = "통계 화면에 필터를 추가해주세요",
				content = "기간별로 필터링할 수 있으면 좋겠어요.",
				email = "user@example.com",
			),
			onTitleChange = {},
			onContentChange = {},
			onEmailChange = {},
			onImagePicked = {},
			onImageRemove = {},
			onSubmit = {},
			onBack = {},
		)
	}
}
