package com.hand.log.utils.share

import androidx.compose.runtime.Composable

expect class ShareManager {
	fun shareText(text: String)
	fun shareImage(imageBytes: ByteArray, fileName: String)
	fun saveImage(imageBytes: ByteArray, fileName: String)
}

@Composable
expect fun rememberShareManager(): ShareManager
