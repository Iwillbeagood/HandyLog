package com.hand.log.platform.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun String.toClipEntry(): ClipEntry =
	ClipData.newPlainText(this, this).toClipEntry()
