package com.hand.log.utils.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toPngBytes(): ByteArray {
	val skiaBitmap = this.asSkiaBitmap()
	val image = Image.makeFromBitmap(skiaBitmap)
	val data = image.encodeToData(EncodedImageFormat.PNG)
		?: throw IllegalStateException("Failed to encode image to PNG")
	return data.bytes
}
