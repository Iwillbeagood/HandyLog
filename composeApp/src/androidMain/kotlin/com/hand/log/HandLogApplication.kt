package com.hand.log

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HandLogApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(
			handLogAppDeclaration {
                androidContext(this@HandLogApplication)
            },
        )
    }
}
