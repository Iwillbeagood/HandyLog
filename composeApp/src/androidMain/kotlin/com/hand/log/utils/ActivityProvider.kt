package com.hand.log.utils

import android.app.Activity

var activityProvider: () -> Activity? = {
	null
}

fun setActivityProvider(provider: () -> Activity?) {
	activityProvider = provider
}
