package com.hand.log.record.quick.model

import androidx.compose.runtime.Immutable
import com.hand.log.domain.model.ActionType
import com.hand.log.domain.model.Card
import com.hand.log.domain.model.HandRecord
import com.hand.log.domain.model.Street
import com.hand.log.domain.model.quick.QuickHandSnapshot
import com.hand.log.domain.model.quick.QuickPending
import com.hand.log.ui.poker.HandHistoryFormatter
import com.hand.log.ui.poker.HandHistoryProgress

internal enum class EditorLineKind {
	HEADER,
	BOARD,
	HERO,
	AGGRESSIVE,
	AUTO,
	NORMAL,
}

@Immutable
internal data class EditorLine(
	val text: String,
	val kind: EditorLineKind,
	val caretTrailing: String? = null,
	val cards: List<Card> = emptyList(),
	val afterCards: String = "",
	val draft: String = "",
) {
	val hasCaret: Boolean get() = caretTrailing != null
}

internal object QuickEditorRenderer {

	fun render(
		snapshot: QuickHandSnapshot,
		finalRecord: HandRecord? = null,
		showBbUnit: Boolean = true,
		draft: List<HandKey> = emptyList(),
	): List<EditorLine> {
		val pending = snapshot.pending
		val record = finalRecord ?: snapshot.record
		val progress = if (finalRecord != null) {
			HandHistoryProgress.Complete
		} else {
			HandHistoryProgress(
				openStreet = snapshot.openStreet,
				isShowdownOpen = snapshot.isShowdownOpen,
				pendingShowdownSeats = snapshot.pendingShowdownSeats,
				isComplete = false,
			)
		}
		val draftText = draft.draftText(snapshot.isBetOpen)
		val lines = HandHistoryFormatter.format(record, progress, showStacks = false)
			.lines()
			.map { it.trimEnd() }
			.filter { it.isNotBlank() }
			.filter { finalRecord != null || !it.startsWith(AUTO_PREFIX) }
			.mapIndexed { index, text -> cardLine(record, index, text) ?: line(text) }
			.toMutableList()
		attachHoleCards(lines, record, snapshot)
		if (pending !is QuickPending.HeroCards) inlineHeroCards(lines)

		when (pending) {
			is QuickPending.Actor -> {
				val actor = if (pending.isChosen) HandHistoryFormatter.formatActor(record, pending.seat) else ""
				lines += line(actor, "", draft = draftText)
			}
			is QuickPending.PlayerCards -> lines += line(
				HandHistoryFormatter.formatActor(record, pending.seat),
				"",
				cards = pending.cards + listOfNotNull(pending.rank?.let { Card(it) }),
				draft = draftText,
			)
			is QuickPending.Amount -> {
				val actor = HandHistoryFormatter.formatActor(record, pending.seat)
				val label = when (pending.type) {
					null -> null
					ActionType.BET -> "bets"
					ActionType.ALL_IN -> "all-in"
					else -> HandHistoryFormatter.formatRaiseLabel(pending.betLevel)
				}
				val unit = if (snapshot.amountInChips || !showBbUnit) "" else BB_UNIT
				val size = pending.potSize?.let { HandHistoryFormatter.formatPotSize(it, snapshot.isBetOpen) }
				val amount = size?.let { "$it (${pending.text}" } ?: pending.text
				lines += line(listOfNotNull(actor, label, amount).joinToString(" "), unit + if (size != null) ")" else "")
			}
			is QuickPending.Staged -> {
				val actor = HandHistoryFormatter.formatActor(record, pending.seat)
				val amount = pending.amount?.let { HandHistoryFormatter.formatBb(it, record.bbAmount) }
				val label = when (pending.type) {
					ActionType.FOLD -> "folds"
					ActionType.CHECK -> "checks"
					ActionType.CALL -> "calls"
					else -> "all-in"
				}
				lines += line(listOfNotNull(actor, label, amount).joinToString(" "), "")
			}
			is QuickPending.BoardCard -> {
				val header = lines.removeLastOrNull() ?: line("")
				lines += header.copy(
					caretTrailing = "",
					cards = header.cards + listOfNotNull(pending.rank?.let { Card(it) }),
					draft = draftText,
				)
			}
			is QuickPending.HeroCards -> {
				val heroLine = line(
					"${pending.position.label} (Hero)",
					"",
					cards = pending.cards + listOfNotNull(pending.rank?.let { Card(it) }),
					draft = draftText,
				)
				if (lines.firstOrNull()?.kind == EditorLineKind.HERO) {
					lines[0] = heroLine
				} else {
					lines.add(
						0,
						heroLine,
					)
				}
			}
			null -> Unit
		}
		return if (showBbUnit) {
			lines
		} else {
			lines.map {
				it.copy(
					text = it.text.replace(BB_SUFFIX, "$1"),
					afterCards = it.afterCards.replace(BB_SUFFIX, "$1"),
				)
			}
		}
	}

	private fun attachHoleCards(lines: MutableList<EditorLine>, record: HandRecord, snapshot: QuickHandSnapshot) {
		snapshot.opponentHands.filter { it.cards.isNotEmpty() }.forEach { hand ->
			val actor = HandHistoryFormatter.formatActor(record, hand.seat)
			val index = lines.indexOfFirst { it.cards.isEmpty() && it.text.startsWith("$actor ") }
			if (index < 0) return@forEach
			val original = lines[index]
			lines[index] = original.copy(
				text = actor,
				cards = hand.cards,
				afterCards = original.text.removePrefix(actor),
			)
		}
	}

	private fun inlineHeroCards(lines: MutableList<EditorLine>) {
		val header = lines.firstOrNull()?.takeIf {
			it.kind == EditorLineKind.HERO && it.cards.isNotEmpty()
		} ?: return
		val index = lines.indexOfFirst { it.cards.isEmpty() && it.text.startsWith("${header.text} ") }
		if (index < 0) return
		val original = lines[index]
		lines[index] = original.copy(
			text = header.text,
			cards = header.cards,
			afterCards = original.text.removePrefix(header.text),
		)
		lines.removeAt(0)
	}

	private fun cardLine(record: HandRecord, index: Int, text: String): EditorLine? {
		STREET_HEADERS.forEach { (header, street) ->
			if (text.startsWith(header)) {
				return EditorLine(
					text = header.removeSurrounding("[", "]"),
					kind = EditorLineKind.BOARD,
					cards = record.streets.getCards(street),
				)
			}
		}
		val heroCards = record.heroHoleCards ?: return null
		if (index != 0 || !HERO_ACTOR.containsMatchIn(text)) return null
		return line(
			text.substringBefore(HERO_TAG) + HERO_TAG,
			cards = listOf(heroCards.card1, heroCards.card2),
		)
	}

	private fun line(
		text: String,
		caretTrailing: String? = null,
		cards: List<Card> = emptyList(),
		draft: String = "",
	) = EditorLine(
		text = text,
		kind = kindOf(text),
		caretTrailing = caretTrailing,
		cards = cards,
		draft = draft,
	)

	private fun kindOf(text: String): EditorLineKind = when {
		text.startsWith("[") -> EditorLineKind.HEADER
		text.startsWith(AUTO_PREFIX) -> EditorLineKind.AUTO
		HERO_ACTOR.containsMatchIn(text) -> EditorLineKind.HERO
		AGGRESSIVE_MARKERS.any { it in text } -> EditorLineKind.AGGRESSIVE
		else -> EditorLineKind.NORMAL
	}

	private val STREET_HEADERS = listOf(
		"[Flop]" to Street.FLOP,
		"[Turn]" to Street.TURN,
		"[River]" to Street.RIVER,
	)

	private const val HERO_TAG = "(Hero)"

	private val HERO_ACTOR = Regex("""^\S+ \(Hero\)""")

	private const val BB_UNIT = "BB"

	private const val AUTO_PREFIX = "→"

	private val BB_SUFFIX = Regex("""(\d)BB\b""")

	private val AGGRESSIVE_MARKERS = listOf(" opens to ", "-bets to ", " raises to ", " bets ")
}
