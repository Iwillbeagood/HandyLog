package com.hand.log.utils.share

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.toPngBytes(): ByteArray {
	val bitmap = this.asAndroidBitmap()
	val stream = ByteArrayOutputStream()
	bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
	return stream.toByteArray()
}
