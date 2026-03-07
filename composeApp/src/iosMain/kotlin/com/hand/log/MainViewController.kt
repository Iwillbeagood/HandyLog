package com.hand.log

import androidx.compose.ui.window.ComposeUIViewController
import com.hand.log.App
import com.hand.log.handLogAppDeclaration
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        application = handLogAppDeclaration(),
    ) {
        App()
    }
}
