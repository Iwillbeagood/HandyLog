package com.hand.log.platform.clipboard

import androidx.compose.ui.platform.ClipEntry

expect fun String.toClipEntry(): ClipEntry
