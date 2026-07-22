package com.hand.log.platform.image

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePicker(onPicked: (PickedImage) -> Unit): ImagePickerLauncher {
	val context = LocalContext.current
	val currentOnPicked = rememberUpdatedState(onPicked)
	val launcher = rememberLauncherForActivityResult(
		ActivityResultContracts.PickVisualMedia(),
	) { uri ->
		uri ?: return@rememberLauncherForActivityResult
		val resolver = context.contentResolver
		val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
			?: return@rememberLauncherForActivityResult
		val mimeType = resolver.getType(uri) ?: "image/jpeg"
		val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "image"
		currentOnPicked.value(PickedImage(bytes, fileName, mimeType))
	}
	return remember {
		ImagePickerLauncher {
			launcher.launch(
				PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
			)
		}
	}
}
