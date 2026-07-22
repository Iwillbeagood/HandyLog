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

		// paid flavor 에 google-services.json 이 아직 없으면 초기화가 null 을 반환한다.
		// 이 경우 Crashlytics 접근이 크래시를 유발하므로 초기화 성공 시에만 활성화한다.
		if (FirebaseApp.initializeApp(this) != null) {
			FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
		}

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
