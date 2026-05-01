package com.hand.log.utils.share

import androidx.compose.runtime.Composable

expect class ShareManager {
	/** @return true if text was copied to clipboard (iOS), false if share sheet was shown (Android) */
	fun shareText(text: String): Boolean
	fun shareImage(imageBytes: ByteArray, fileName: String)
	fun saveImage(imageBytes: ByteArray, fileName: String)
}

@Composable
expect fun rememberShareManager(): ShareManager
