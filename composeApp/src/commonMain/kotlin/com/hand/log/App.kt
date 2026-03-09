package com.hand.log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import base_app.composeapp.generated.resources.NotoSans
import base_app.composeapp.generated.resources.Res
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.home.di.featureHomeModule
import com.hand.log.main.MainScreen
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.MainActionInterop
import com.hand.log.toast.ToastDurationType
import com.hand.log.toast.ToastManager
import org.jetbrains.compose.resources.Font
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Composable
internal fun App(
	fontFamily: FontFamily = FontFamily(Font(resource = Res.font.NotoSans)),
) {
	val toastManager by remember { mutableStateOf(ToastManager()) }

	val mainActionInterop = object : MainActionInterop {
		override fun onFinish() { }
		override fun onRestart() { }
		override fun onShowToast(message: String, toastDurationType: ToastDurationType) {
			toastManager.showToast(message, toastDurationType)
		}
	}

	CompositionLocalProvider(
		LocalMainActionInterop provides mainActionInterop,
	) {
		HandLogTheme {
			MainScreen()
		}
	}
}

internal val appModule = module {
	// :core:data
//    includes(
//        coreDataSettingModule,
//        coreDataSessionModule,
//        coreDataContributorModule,
//        coreRouterModule,
//    )
//
//    // :core:datastore
//    includes(coreDatastoreCoreModules)
//    includes(
//        coreDatastoreSessionModule,
//        coreDatastoreSettingsModule,
//    )
//    // :core:domain
//    includes(
//        coreDomainSessionModule,
//        coreDomainContributorModule,
//    )
//    // :core:network
//    includes(
//        coreNetworkModule,
//    )
	// :feature
	includes(
		featureHomeModule,
	)
}

internal fun handLogAppDeclaration(
	additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
	modules(appModule)
	additionalDeclaration()
}
