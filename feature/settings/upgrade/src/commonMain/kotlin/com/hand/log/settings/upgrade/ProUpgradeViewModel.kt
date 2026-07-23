package com.hand.log.settings.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.billing.PurchaseResult
import com.hand.log.domain.usecase.ObserveProStatusUseCase
import com.hand.log.domain.usecase.PurchaseProUseCase
import com.hand.log.domain.usecase.RestorePurchasesUseCase
import com.hand.log.settings.upgrade.contract.ProUpgradeEffect
import com.hand.log.settings.upgrade.contract.ProUpgradeState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ProUpgradeViewModel(
	observeProStatus: ObserveProStatusUseCase,
	private val purchasePro: PurchaseProUseCase,
	private val restorePurchases: RestorePurchasesUseCase,
) : ViewModel() {

	private val _state = MutableStateFlow(ProUpgradeState())
	val state: StateFlow<ProUpgradeState> = _state

	private val _effect = MutableSharedFlow<ProUpgradeEffect>()
	val effect: SharedFlow<ProUpgradeEffect> = _effect.asSharedFlow()

	init {
		viewModelScope.launch {
			observeProStatus().collect { isPro ->
				_state.update { it.copy(isPro = isPro) }
			}
		}
		viewModelScope.launch {
			_state.update { it.copy(price = purchasePro.product()?.formattedPrice) }
		}
	}

	fun onPurchase() {
		if (_state.value.isProcessing) return
		viewModelScope.launch {
			_state.update { it.copy(isProcessing = true) }
			val result = purchasePro()
			_state.update { it.copy(isProcessing = false) }
			result.emitEffect()
		}
	}

	fun onRestore() {
		if (_state.value.isProcessing) return
		viewModelScope.launch {
			_state.update { it.copy(isProcessing = true) }
			val result = restorePurchases()
			_state.update { it.copy(isProcessing = false) }
			result.emitEffect()
		}
	}

	private suspend fun PurchaseResult.emitEffect() {
		when (this) {
			PurchaseResult.Success -> _effect.emit(ProUpgradeEffect.Success)
			PurchaseResult.AlreadyOwned -> _effect.emit(ProUpgradeEffect.AlreadyOwned)
			PurchaseResult.Pending -> _effect.emit(ProUpgradeEffect.Pending)
			PurchaseResult.NothingToRestore -> _effect.emit(ProUpgradeEffect.NothingToRestore)
			is PurchaseResult.Failure -> _effect.emit(ProUpgradeEffect.Failure)
			PurchaseResult.Cancelled -> Unit // 사용자가 취소 — 별도 안내 없음
		}
	}
}
