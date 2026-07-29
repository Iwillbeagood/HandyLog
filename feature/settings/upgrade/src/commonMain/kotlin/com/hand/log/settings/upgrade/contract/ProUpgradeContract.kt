package com.hand.log.settings.upgrade.contract

internal data class ProUpgradeState(
	val isPro: Boolean = false,
	val price: String? = null,
	val isProcessing: Boolean = false,
)

internal sealed interface ProUpgradeEffect {
	data object Success : ProUpgradeEffect
	data object AlreadyOwned : ProUpgradeEffect
	data object Pending : ProUpgradeEffect
	data object NothingToRestore : ProUpgradeEffect
	data object Failure : ProUpgradeEffect
}
