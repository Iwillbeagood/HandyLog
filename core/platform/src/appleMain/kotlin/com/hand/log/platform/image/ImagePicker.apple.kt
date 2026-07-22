package com.hand.log.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberImagePicker(onPicked: (PickedImage) -> Unit): ImagePickerLauncher {
	val currentOnPicked = rememberUpdatedState(onPicked)
	return remember {
		ImagePickerLauncher {
			presentImagePicker { picked ->
				if (picked != null) currentOnPicked.value(picked)
			}
		}
	}
}

// 델리게이트가 조기 해제되지 않도록 표시 중인 동안 강한 참조를 유지한다.
private val activeDelegates = mutableListOf<PhotoPickerDelegate>()

private fun presentImagePicker(onResult: (PickedImage?) -> Unit) {
	val presenter = topViewController() ?: run {
		onResult(null)
		return
	}
	val picker = UIImagePickerController()
	val delegate = PhotoPickerDelegate(onResult)
	activeDelegates.add(delegate)
	picker.delegate = delegate
	presenter.presentViewController(picker, animated = true, completion = null)
}

private class PhotoPickerDelegate(
	private val onResult: (PickedImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

	override fun imagePickerController(
		picker: UIImagePickerController,
		didFinishPickingMediaWithInfo: Map<Any?, *>,
	) {
		val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
		picker.dismissViewControllerAnimated(true) {
			finish(image?.toPickedImage())
		}
	}

	override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
		picker.dismissViewControllerAnimated(true) {
			finish(null)
		}
	}

	private fun finish(result: PickedImage?) {
		onResult(result)
		activeDelegates.remove(this)
	}
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toPickedImage(): PickedImage? {
	val data = UIImageJPEGRepresentation(this, 0.9) ?: return null
	return PickedImage(
		bytes = data.toByteArray(),
		fileName = "image.jpg",
		mimeType = "image/jpeg",
	)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
	val size = length.toInt()
	if (size == 0) return ByteArray(0)
	val result = ByteArray(size)
	result.usePinned { pinned ->
		memcpy(pinned.addressOf(0), bytes, length)
	}
	return result
}

private fun topViewController(): UIViewController? {
	val window = keyWindow() ?: return null
	var vc = window.rootViewController ?: return null
	while (true) {
		vc = vc.presentedViewController ?: break
	}
	return vc
}

private fun keyWindow(): UIWindow? {
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
