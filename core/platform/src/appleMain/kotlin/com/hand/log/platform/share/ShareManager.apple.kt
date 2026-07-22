package com.hand.log.platform.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIPasteboard
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class ShareManager {
	actual fun shareText(text: String): Boolean {
		UIPasteboard.generalPasteboard.string = text
		return true
	}

	@OptIn(ExperimentalForeignApi::class)
	actual fun shareImage(imageBytes: ByteArray, fileName: String) {
		val nsData = imageBytes.usePinned {
			NSData.dataWithBytes(it.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage(data = nsData)

		dispatch_async(dispatch_get_main_queue()) {
			val presenter = findTopViewController() ?: return@dispatch_async
			val activityVC = UIActivityViewController(
				activityItems = listOf(image),
				applicationActivities = null,
			)
			activityVC.popoverPresentationController()?.sourceView = presenter.view
			presenter.presentViewController(activityVC, animated = true, completion = null)
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	actual fun saveImage(imageBytes: ByteArray, fileName: String) {
		val nsData = imageBytes.usePinned {
			NSData.dataWithBytes(it.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage(data = nsData)
		UIImageWriteToSavedPhotosAlbum(image, null, null, null)
	}

	private fun findTopViewController(): UIViewController? {
		val window = findKeyWindow() ?: return null
		var vc = window.rootViewController ?: return null
		while (true) {
			vc = vc.presentedViewController ?: break
		}
		return vc
	}

	private fun findKeyWindow(): UIWindow? {
		val scenes = UIApplication.sharedApplication.connectedScenes
		for (scene in scenes) {
			if (scene is UIWindowScene) {
				for (window in scene.windows) {
					if ((window as? UIWindow)?.isKeyWindow() == true) {
						return window
					}
				}
				return scene.windows.firstOrNull() as? UIWindow
			}
		}
		return null
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	return remember { ShareManager() }
}
