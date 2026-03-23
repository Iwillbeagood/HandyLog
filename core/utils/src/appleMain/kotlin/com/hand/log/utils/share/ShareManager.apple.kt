package com.hand.log.utils.share

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
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

actual class ShareManager {
	actual fun shareText(text: String) {
		val topVC = findTopViewController() ?: return
		val activityVC = UIActivityViewController(
			activityItems = listOf(text),
			applicationActivities = null,
		)
		activityVC.popoverPresentationController()?.sourceView = topVC.view
		topVC.presentViewController(activityVC, animated = true, completion = null)
	}

	@OptIn(ExperimentalForeignApi::class)
	actual fun shareImage(imageBytes: ByteArray, fileName: String) {
		val topVC = findTopViewController() ?: return

		val nsData = imageBytes.usePinned {
			NSData.dataWithBytes(it.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage(data = nsData) ?: return

		val activityVC = UIActivityViewController(
			activityItems = listOf(image),
			applicationActivities = null,
		)
		activityVC.popoverPresentationController()?.sourceView = topVC.view
		topVC.presentViewController(activityVC, animated = true, completion = null)
	}

	private fun findTopViewController(): UIViewController? {
		val scenes = UIApplication.sharedApplication.connectedScenes
		var windowScene: UIWindowScene? = null
		for (scene in scenes) {
			if (scene is UIWindowScene) {
				windowScene = scene
				break
			}
		}
		if (windowScene == null) return null

		var keyWindow: UIWindow? = null
		for (w in windowScene.windows) {
			if ((w as? UIWindow)?.isKeyWindow() == true) {
				keyWindow = w as UIWindow
				break
			}
		}
		if (keyWindow == null) {
			keyWindow = windowScene.windows.firstOrNull() as? UIWindow
		}

		var topVC = keyWindow?.rootViewController ?: return null
		while (topVC.presentedViewController != null) {
			topVC = topVC.presentedViewController!!
		}
		return topVC
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	return remember { ShareManager() }
}
