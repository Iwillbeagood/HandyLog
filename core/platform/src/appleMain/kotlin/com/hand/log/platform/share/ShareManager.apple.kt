package com.hand.log.platform.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIPasteboard
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

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
	actual suspend fun saveImage(imageBytes: ByteArray, fileName: String): Boolean {
		val nsData = imageBytes.usePinned {
			NSData.dataWithBytes(it.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage(data = nsData)
		if (!requestAddOnlyAuthorization()) return false
		return suspendCancellableCoroutine { continuation ->
			PHPhotoLibrary.sharedPhotoLibrary().performChanges(
				changeBlock = { PHAssetChangeRequest.creationRequestForAssetFromImage(image) },
				completionHandler = { success, _ -> continuation.resume(success) },
			)
		}
	}

	private suspend fun requestAddOnlyAuthorization(): Boolean {
		val current = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
		val status = if (current == PHAuthorizationStatusNotDetermined) {
			suspendCancellableCoroutine { continuation ->
				PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { result ->
					continuation.resume(result)
				}
			}
		} else {
			current
		}
		return status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
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
