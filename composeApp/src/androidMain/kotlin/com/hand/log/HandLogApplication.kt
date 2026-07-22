package com.hand.log

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hand.log.common.AppConfig
import com.hand.log.platform.etc.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HandLogApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		AppConfig.initialize(isProBuild = BuildConfig.IS_PRO)

		FirebaseApp.initializeApp(this)
		FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

		setupGlobalExceptionHandlers()

		startKoin(
			handLogAppDeclaration {
				androidContext(this@HandLogApplication)
			},
		)
	}

	private fun setupGlobalExceptionHandlers() {
		val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
		Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
			Logger.e("UncaughtException on thread=${thread.name}", throwable)
			defaultHandler?.uncaughtException(thread, throwable)
		}
	}
}
