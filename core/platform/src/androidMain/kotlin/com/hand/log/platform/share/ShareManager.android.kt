package com.hand.log.platform.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class ShareManager(private val context: Context) {
	actual fun shareText(text: String): Boolean {
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, text)
			flags += Intent.FLAG_ACTIVITY_NEW_TASK
		}
		context.startActivity(Intent.createChooser(intent, null))
		return false
	}

	actual fun shareImage(imageBytes: ByteArray, fileName: String) {
		val file = File(context.cacheDir, fileName)
		file.writeBytes(imageBytes)
		val uri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.provider",
			file,
		)
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "image/png"
			putExtra(Intent.EXTRA_STREAM, uri)
			flags += Intent.FLAG_ACTIVITY_NEW_TASK
			flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
		}
		context.startActivity(Intent.createChooser(intent, null))
	}

	actual fun saveImage(imageBytes: ByteArray, fileName: String) {
		val contentValues = ContentValues().apply {
			put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
			put(MediaStore.Images.Media.MIME_TYPE, "image/png")
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HandyLog")
				put(MediaStore.Images.Media.IS_PENDING, 1)
			}
		}
		val resolver = context.contentResolver
		val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return
		resolver.openOutputStream(uri)?.use { it.write(imageBytes) }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			contentValues.clear()
			contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
			resolver.update(uri, contentValues, null, null)
		}
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	val context = LocalContext.current
	return remember(context) { ShareManager(context) }
}
