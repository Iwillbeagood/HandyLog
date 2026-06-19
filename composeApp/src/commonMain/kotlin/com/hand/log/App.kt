package com.hand.log

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.data.datasoure.di.networkModule
import com.hand.log.data.datasoure.di.remoteDataSourceModule
import com.hand.log.data.repositoryImpl.di.repositoryModule
import com.hand.log.database.di.databaseDataSourceModule
import com.hand.log.database.di.databaseModule
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.domain.model.ThemeMode
import com.hand.log.domain.model.etc.ToastDurationType
import com.hand.log.domain.repository.AppSettingsRepository
import com.hand.log.handdetail.di.featureHandDetailModule
import com.hand.log.home.di.featureHomeModule
import com.hand.log.local.datastore.di.dataStoreDataSourceModule
import com.hand.log.local.datastore.di.dataStoreModule
import com.hand.log.main.MainScreen
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.MainActionInterop
import com.hand.log.players.di.featurePlayersModule
import com.hand.log.players.hands.di.featurePlayerHandsModule
import com.hand.log.playersetup.di.featurePlayerSetupModule
import com.hand.log.record.di.featureRecordModule
import com.hand.log.settings.betsize.di.featureSettingsBetSizeModule
import com.hand.log.settings.contact.di.featureSettingsContactModule
import com.hand.log.settings.main.di.featureSettingsMainModule
import com.hand.log.table.di.featureTableModule
import com.hand.log.tableedit.di.featureTableEditModule
import com.hand.log.utils.NavigationBarEffect
import com.hand.log.utils.StatusBarEffect
import com.hand.log.utils.toast.ToastManager
import org.koin.compose.koinInject
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Composable
internal fun App() {
	val toastManager by remember { mutableStateOf(ToastManager()) }

	val mainActionInterop = object : MainActionInterop {
		override fun onFinish() {}
		override fun onRestart() {}
		override fun onShowToast(message: String, toastDurationType: ToastDurationType) {
			toastManager.showToast(message, toastDurationType)
		}
	}

	val appSettingsRepository: AppSettingsRepository = koinInject()
	val themeMode by appSettingsRepository.observeThemeMode()
		.collectAsStateWithLifecycle(initialValue = ThemeMode.AUTO)
	val darkTheme = when (themeMode) {
		ThemeMode.AUTO -> isSystemInDarkTheme()
		ThemeMode.LIGHT -> false
		ThemeMode.DARK -> true
	}

	CompositionLocalProvider(
		LocalMainActionInterop provides mainActionInterop,
	) {
		HandLogTheme(darkTheme = darkTheme) {
			StatusBarEffect(
				isDarkTheme = darkTheme,
				backgroundColor = HandyTheme.colorScheme.background,
			)
			NavigationBarEffect(
				backgroundColor = HandyTheme.colorScheme.card,
			)

			MainScreen()
		}
	}
}

internal val appModule = module {
	includes(
		databaseModule,
		databaseDataSourceModule,
		dataStoreModule,
		dataStoreDataSourceModule,
		networkModule,
		remoteDataSourceModule,
		repositoryModule,
	)
	includes(
		featureHomeModule,
		featureTableModule,
		featureTableEditModule,
		featureRecordModule,
		featureHandDetailModule,
		featurePlayersModule,
		featurePlayerHandsModule,
		featurePlayerSetupModule,
		featureSettingsMainModule,
		featureSettingsBetSizeModule,
		featureSettingsContactModule,
	)
}

internal fun handLogAppDeclaration(
	additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
	modules(appModule)
	additionalDeclaration()
}
