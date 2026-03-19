package com.hand.log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.hand.log.data.datasoure.di.dataSourceModule
import com.hand.log.data.repositoryImpl.di.repositoryModule
import com.hand.log.database.di.databaseModule
import com.hand.log.designsystem.theme.HandLogTheme
import com.hand.log.domain.model.etc.ToastDurationType
import com.hand.log.home.di.featureHomeModule
import com.hand.log.main.MainScreen
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.MainActionInterop
import com.hand.log.handdetail.di.featureHandDetailModule
import com.hand.log.players.di.featurePlayersModule
import com.hand.log.record.di.featureRecordModule
import com.hand.log.table.di.featureTableModule
import com.hand.log.utils.toast.ToastManager
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

	CompositionLocalProvider(
		LocalMainActionInterop provides mainActionInterop,
	) {
		HandLogTheme {
			MainScreen()
		}
	}
}

internal val appModule = module {
	includes(
		databaseModule,
		dataSourceModule,
		repositoryModule,
	)
	includes(
		featureHomeModule,
		featureTableModule,
		featureRecordModule,
		featureHandDetailModule,
		featurePlayersModule,
	)
}

internal fun handLogAppDeclaration(
	additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
	modules(appModule)
	additionalDeclaration()
}
