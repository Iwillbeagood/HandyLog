package com.hand.log.settings.upgrade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hand.log.navigation.interop.LocalMainActionInterop
import com.hand.log.navigation.interop.LocalNavigateActionInterop
import com.hand.log.settings.upgrade.contract.ProUpgradeEffect
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.purchase_already_owned
import handylog.core.res.generated.resources.purchase_error
import handylog.core.res.generated.resources.purchase_pending
import handylog.core.res.generated.resources.purchase_restore_none
import handylog.core.res.generated.resources.purchase_success

@Composable
internal fun ProUpgradeRoute(
	viewModel: ProUpgradeViewModel,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val mainAction = LocalMainActionInterop.current

	LaunchedEffect(Unit) {
		viewModel.effect.collect { effect ->
			val message = when (effect) {
				ProUpgradeEffect.Success -> Res.string.purchase_success
				ProUpgradeEffect.AlreadyOwned -> Res.string.purchase_already_owned
				ProUpgradeEffect.Pending -> Res.string.purchase_pending
				ProUpgradeEffect.NothingToRestore -> Res.string.purchase_restore_none
				ProUpgradeEffect.Failure -> Res.string.purchase_error
			}
			mainAction.onShowToast(message)
		}
	}

	ProUpgradeScreen(
		state = state,
		onBack = navAction::popBackStack,
		onPurchase = viewModel::onPurchase,
		onRestore = viewModel::onRestore,
	)
}
