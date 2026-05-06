package com.hand.log.utils.toast

import com.hand.log.domain.model.etc.ToastDurationType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIScreen
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionCurveEaseOut
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual open class ToastManager actual constructor() {
	private var toastWindow: UIWindow? = null

	@OptIn(ExperimentalForeignApi::class)
	actual fun showToast(message: String, toastDurationType: ToastDurationType) {
		dispatch_async(dispatch_get_main_queue()) {
			showToastInternal(message, toastDurationType)
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	private fun showToastInternal(message: String, toastDurationType: ToastDurationType) {
		val (displayDuration, fadeDuration) = when (toastDurationType) {
			ToastDurationType.SHORT -> 1.5 to 0.5
			ToastDurationType.LONG -> 3.0 to 0.8
		}

		val windowScene = findWindowScene() ?: return

		val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
		val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }

		// 기존 토스트 윈도우 정리
		toastWindow?.setHidden(true)
		toastWindow = null

		// 토스트 전용 윈도우 생성 (다른 UI 변화에 영향 받지 않음)
		val window = UIWindow(windowScene = windowScene)
		window.windowLevel = UIWindowLevelAlert + 1.0
		window.setUserInteractionEnabled(false)
		window.backgroundColor = UIColor.clearColor

		val toastLabel = UILabel(
			frame = CGRectMake(0.0, 0.0, screenWidth - 60, 40.0),
		)
		toastLabel.center = CGPointMake(screenWidth / 2, screenHeight - 120.0)
		toastLabel.textAlignment = NSTextAlignmentCenter
		toastLabel.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.75)
		toastLabel.textColor = UIColor.whiteColor
		toastLabel.font = UIFont.systemFontOfSize(14.0)
		toastLabel.text = message
		toastLabel.alpha = 1.0
		toastLabel.layer.cornerRadius = 20.0
		toastLabel.clipsToBounds = true

		window.addSubview(toastLabel)
		window.setHidden(false)
		toastWindow = window

		UIView.animateWithDuration(
			duration = fadeDuration,
			delay = displayDuration,
			options = UIViewAnimationOptionCurveEaseOut,
			animations = {
				toastLabel.alpha = 0.0
			},
			completion = { finished ->
				if (finished) {
					window.setHidden(true)
					if (toastWindow == window) {
						toastWindow = null
					}
				}
			},
		)
	}

	private fun findWindowScene(): UIWindowScene? {
		val scenes = UIApplication.sharedApplication.connectedScenes
		for (scene in scenes) {
			if (scene is UIWindowScene) {
				return scene
			}
		}
		return null
	}
}
