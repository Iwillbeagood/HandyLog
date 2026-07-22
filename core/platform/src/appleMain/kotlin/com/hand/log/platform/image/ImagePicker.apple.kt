package com.hand.log.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
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

/**
 * PHPickerViewController 는 앱 프로세스 밖에서 동작해 사진 라이브러리 접근 권한이 필요 없다.
 * (UIImagePickerController 와 달리 Info.plist 사용 설명 문자열도 요구하지 않는다.)
 */
private fun presentImagePicker(onResult: (PickedImage?) -> Unit) {
	val presenter = topViewController() ?: run {
		onResult(null)
		return
	}
	val configuration = PHPickerConfiguration().apply {
		selectionLimit = 1
		filter = PHPickerFilter.imagesFilter()
	}
	val picker = PHPickerViewController(configuration = configuration)
	val delegate = PhotoPickerDelegate(onResult)
	activeDelegates.add(delegate)
	picker.delegate = delegate
	presenter.presentViewController(picker, animated = true, completion = null)
}

private class PhotoPickerDelegate(
	private val onResult: (PickedImage?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

	override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
		picker.dismissViewControllerAnimated(true, null)

		val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
		if (provider == null) {
			finish(null)
			return
		}

		provider.loadDataRepresentationForTypeIdentifier(
			PUBLIC_IMAGE_TYPE,
		) { data: NSData?, _: NSError? ->
			val picked = data?.let { UIImage(data = it)?.toPickedImage() }
			// 데이터 로드 콜백은 임의 큐에서 호출될 수 있어 UI 상태 전달은 메인 큐로 넘긴다.
			dispatch_async(dispatch_get_main_queue()) {
				finish(picked)
			}
		}
	}

	private fun finish(result: PickedImage?) {
		onResult(result)
		activeDelegates.remove(this)
	}
}

private const val PUBLIC_IMAGE_TYPE = "public.image"

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
