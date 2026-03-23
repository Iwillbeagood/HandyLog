package com.hand.log.utils.share

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class ShareManager(private val context: Context) {
	actual fun shareText(text: String) {
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, text)
			flags += Intent.FLAG_ACTIVITY_NEW_TASK
		}
		context.startActivity(Intent.createChooser(intent, null))
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
}

@Composable
actual fun rememberShareManager(): ShareManager {
	val context = LocalContext.current
	return remember(context) { ShareManager(context) }
}
