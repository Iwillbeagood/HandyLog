package com.hand.log.platform.image

import androidx.compose.runtime.Composable

/**
 * 갤러리에서 선택한 이미지의 원본 바이트와 메타데이터.
 */
class PickedImage(
	val bytes: ByteArray,
	val fileName: String,
	val mimeType: String,
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is PickedImage) return false
		return fileName == other.fileName &&
			mimeType == other.mimeType &&
			bytes.contentEquals(other.bytes)
	}

	override fun hashCode(): Int {
		var result = bytes.contentHashCode()
		result = 31 * result + fileName.hashCode()
		result = 31 * result + mimeType.hashCode()
		return result
	}
}

/**
 * 시스템 사진 선택 UI를 여는 런처. [launch] 호출 시 갤러리가 열린다.
 */
class ImagePickerLauncher internal constructor(private val onLaunch: () -> Unit) {
	fun launch() = onLaunch()
}

/**
 * 갤러리에서 이미지 한 장을 선택하는 런처를 remember 한다.
 * 사용자가 이미지를 고르면 [onPicked] 로 원본 바이트가 전달되고, 취소하면 호출되지 않는다.
 */
@Composable
expect fun rememberImagePicker(onPicked: (PickedImage) -> Unit): ImagePickerLauncher
