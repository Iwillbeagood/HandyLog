package com.hand.log.platform.share

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class ShareManager(
	private val context: Context,
	private val requestWritePermission: suspend () -> Boolean,
) {
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

	actual suspend fun saveImage(imageBytes: ByteArray, fileName: String): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasWritePermission() && !requestWritePermission()) {
			return false
		}
		return withContext(Dispatchers.IO) { writeToMediaStore(imageBytes, fileName) }
	}

	private fun hasWritePermission(): Boolean =
		ContextCompat.checkSelfPermission(
			context,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
		) == PackageManager.PERMISSION_GRANTED

	private fun writeToMediaStore(imageBytes: ByteArray, fileName: String): Boolean {
		val contentValues = ContentValues().apply {
			put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
			put(MediaStore.Images.Media.MIME_TYPE, "image/png")
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HandyLog")
				put(MediaStore.Images.Media.IS_PENDING, 1)
			}
		}
		val resolver = context.contentResolver
		val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false
		val written = runCatching {
			resolver.openOutputStream(uri)?.use { it.write(imageBytes) } != null
		}.getOrDefault(false)
		if (!written) {
			resolver.delete(uri, null, null)
			return false
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			contentValues.clear()
			contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
			resolver.update(uri, contentValues, null, null)
		}
		return true
	}
}

private class WritePermissionRequester {
	private var pending: CompletableDeferred<Boolean>? = null

	fun onResult(granted: Boolean) {
		pending?.complete(granted)
		pending = null
	}

	suspend fun request(launcher: ActivityResultLauncher<String>): Boolean {
		val deferred = CompletableDeferred<Boolean>()
		pending = deferred
		launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		return deferred.await()
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	val context = LocalContext.current
	val requester = remember { WritePermissionRequester() }
	val launcher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission(),
		requester::onResult,
	)
	return remember(context, launcher) {
		ShareManager(context) { requester.request(launcher) }
	}
}
