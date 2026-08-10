package com.hand.log.handdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.PocketCards
import com.hand.log.domain.model.SavedPlayer
import com.hand.log.domain.repository.HandRecordRepository
import com.hand.log.domain.repository.AiReviewRepository
import com.hand.log.domain.usecase.MarkPlayerOnHandUseCase
import com.hand.log.handdetail.contract.HandDetailEffect
import com.hand.log.handdetail.contract.HandDetailModalEffect
import com.hand.log.handdetail.contract.HandDetailState
import com.hand.log.handdetail.contract.HandReviewStatus
import com.hand.log.handdetail.model.HandHistoryFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HandDetailViewModel(
	handId: String,
	private val handRecordRepository: HandRecordRepository,
	private val markPlayerOnHandUseCase: MarkPlayerOnHandUseCase,
	private val aiReviewRepository: AiReviewRepository,
) : ViewModel() {

	private val useBbUnit = MutableStateFlow(false)
	private val _reviewStatus = MutableStateFlow(HandReviewStatus.IDLE)
	private val _reviewText = MutableStateFlow("")

	private val _effect = MutableSharedFlow<HandDetailEffect>()
	val effect: SharedFlow<HandDetailEffect> get() = _effect.asSharedFlow()

	private val _modalEffect = MutableStateFlow<HandDetailModalEffect>(HandDetailModalEffect.Idle)
	val modalEffect: StateFlow<HandDetailModalEffect> get() = _modalEffect

	val state: StateFlow<HandDetailState> = combine(
		handRecordRepository.observeHandById(handId),
		useBbUnit,
		_reviewStatus,
		_reviewText,
	) { hand, bbUnit, reviewStatus, reviewText ->
		if (hand != null) {
			HandDetailState.Detail(
				hand = hand,
				useBbUnit = bbUnit,
				reviewStatus = reviewStatus,
				reviewText = reviewText,
			)
		} else {
			HandDetailState.Error
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = HandDetailState.Loading,
	)

	fun toggleBbUnit() {
		useBbUnit.value = !useBbUnit.value
	}

	fun showDeleteConfirm() {
		_modalEffect.value = HandDetailModalEffect.ConfirmDelete
	}

	fun confirmDelete() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		dismissModal()
		viewModelScope.launch {
			handRecordRepository.deleteHand(loaded.hand.id) {
				viewModelScope.launch {
					_effect.emit(HandDetailEffect.HandDeleted)
				}
			}
		}
	}

	fun onPlayerClick(seat: Int) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.NavigateToPlayers(loaded.hand.tableId, seat))
		}
	}

	fun showPlayerMark(seat: Int) {
		_modalEffect.value = HandDetailModalEffect.ShowPlayerMark(seat)
	}

	fun saveAndMarkPlayer(player: SavedPlayer, seat: Int) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		dismissModal()
		viewModelScope.launch {
			markPlayerOnHandUseCase(player, loaded.hand.id, seat)
		}
	}

	private fun HandRecord.usedCards(): Set<Card> = buildSet {
		heroHoleCards?.let {
			add(it.card1)
			add(it.card2)
		}
		addAll(streets.boardCards)
		showdown.forEach {
			add(it.card1)
			add(it.card2)
		}
	}

	fun editHeroHand() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		val heroCards = loaded.hand.heroHoleCards?.let { setOf(it.card1, it.card2) } ?: emptySet()
		val usedCards = loaded.hand.usedCards() - heroCards
		_modalEffect.value = HandDetailModalEffect.EditHeroHand(selectedCards = usedCards)
	}

	fun editShowdownHand(seat: Int) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		val existingEntry = loaded.hand.showdown.find { it.seat == seat }
		val existingCards = existingEntry?.cards?.let { setOf(it.card1, it.card2) } ?: emptySet()
		val usedCards = loaded.hand.usedCards() - existingCards
		val positionName = loaded.hand.getPositionName(seat)
		_modalEffect.value = HandDetailModalEffect.EditShowdownHand(
			seat = seat,
			positionName = positionName,
			selectedCards = usedCards,
		)
	}

	fun onCardsSelected(cards: List<Card>) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		val modal = _modalEffect.value
		dismissModal()
		if (cards.size < 2) return

		val newHand = PocketCards(cards[0], cards[1])
		viewModelScope.launch {
			val seat = when (modal) {
				is HandDetailModalEffect.EditHeroHand -> loaded.hand.heroSeat
				is HandDetailModalEffect.EditShowdownHand -> modal.seat
				else -> return@launch
			}
			val updatedPlayers = loaded.hand.players.map { p ->
				if (p.seat == seat) p.copy(cards = newHand) else p
			}
			val updated = loaded.hand.copy(players = updatedPlayers)
			handRecordRepository.saveHand(updated)
		}
	}

	fun showMemoEdit() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		_modalEffect.value = HandDetailModalEffect.EditMemo(
			currentMemo = loaded.hand.memo.orEmpty(),
		)
	}

	fun dismissModal() {
		_modalEffect.value = HandDetailModalEffect.Idle
	}

	fun saveMemo(text: String) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		if (text == (loaded.hand.memo.orEmpty())) return
		viewModelScope.launch {
			val updated = loaded.hand.copy(memo = text.ifBlank { null })
			handRecordRepository.saveHand(updated)
		}
	}

	fun shareText() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		val text = HandHistoryFormatter.format(loaded.hand)
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.ShareText(text))
		}
	}

	fun requestReview(languageName: String) {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		if (_reviewStatus.value == HandReviewStatus.LOADING || _reviewStatus.value == HandReviewStatus.LOADED) return
		val handHistory = HandHistoryFormatter.format(loaded.hand)
		_reviewStatus.value = HandReviewStatus.LOADING
		viewModelScope.launch {
			runCatching { aiReviewRepository.reviewHand(handHistory, languageName) }
				.mapCatching { it.ifBlank { error("empty review") } }
				.fold(
					onSuccess = {
						_reviewText.value = it
						_reviewStatus.value = HandReviewStatus.LOADED
					},
					onFailure = {
						_reviewStatus.value = HandReviewStatus.ERROR
					},
				)
		}
	}

	fun shareImage() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.ShareImage("hand_${loaded.hand.id}.png"))
		}
	}

	fun downloadImage() {
		val loaded = (state.value as? HandDetailState.Detail) ?: return
		viewModelScope.launch {
			_effect.emit(HandDetailEffect.DownloadImage("hand_${loaded.hand.id}.png"))
		}
	}
}
