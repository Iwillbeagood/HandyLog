package com.hand.log.utils.etc

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

actual object AppLogger {

	actual fun e(tag: String, message: String, throwable: Throwable?) {
		if (throwable != null) {
			Log.e(tag, message, throwable)
			FirebaseCrashlytics.getInstance().log("E/$tag: $message")
			FirebaseCrashlytics.getInstance().recordException(throwable)
		} else {
			Log.e(tag, message)
			FirebaseCrashlytics.getInstance().log("E/$tag: $message")
		}
	}

	actual fun d(tag: String, message: String) {
		Log.d(tag, message)
	}

	actual fun i(tag: String, message: String) {
		Log.i(tag, message)
	}
}
