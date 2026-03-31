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
import platform.UIKit.UIWindowScene

actual open class ToastManager actual constructor() {
	@OptIn(ExperimentalForeignApi::class)
	actual fun showToast(message: String, toastDurationType: ToastDurationType) {

		val (displayDuration, fadeDuration) = when (toastDurationType) {
			ToastDurationType.SHORT -> 1.5 to 0.5
			ToastDurationType.LONG -> 3.0 to 0.8
		}

		val keyWindow = findKeyWindow() ?: return
		val rootView = keyWindow.rootViewController?.view ?: return

		val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
		val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }

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
		rootView.addSubview(toastLabel)

		UIView.animateWithDuration(
			duration = fadeDuration,
			delay = displayDuration,
			options = UIViewAnimationOptionCurveEaseOut,
			animations = {
				toastLabel.alpha = 0.0
			},
			completion = {
				if (it) {
					toastLabel.removeFromSuperview()
				}
			},
		)
	}

	private fun findKeyWindow(): UIWindow? {
		val scenes = UIApplication.sharedApplication.connectedScenes
		for (scene in scenes) {
			if (scene is UIWindowScene) {
				for (window in scene.windows) {
					if ((window as? UIWindow)?.isKeyWindow() == true) {
						return window as UIWindow
					}
				}
				return scene.windows.firstOrNull() as? UIWindow
			}
		}
		return null
	}
}
