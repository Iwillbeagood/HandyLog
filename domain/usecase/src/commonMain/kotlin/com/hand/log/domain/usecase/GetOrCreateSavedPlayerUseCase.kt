package com.hand.log.domain.usecase

import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.SavedPlayerRepository

class GetOrCreateSavedPlayerUseCase(
	private val savedPlayerRepository: SavedPlayerRepository,
	private val checkFeatureLimit: CheckFeatureLimitUseCase,
) {

	suspend operator fun invoke(player: SavedPlayer): Result {
		val existing = player.id.takeIf { it.isNotBlank() }?.let { savedPlayerRepository.getPlayerById(it) }
			?: savedPlayerRepository.getPlayerByName(player.name)
		if (existing != null) return Result.Found(existing)
		if (!checkFeatureLimit.canSavePlayer()) return Result.LimitReached
		savedPlayerRepository.savePlayer(player.copy(id = ""))
		val created = savedPlayerRepository.getPlayerByName(player.name) ?: return Result.Failed
		return Result.Found(created)
	}

	sealed interface Result {
		data class Found(val player: SavedPlayer) : Result
		data object LimitReached : Result
		data object Failed : Result
	}
}
