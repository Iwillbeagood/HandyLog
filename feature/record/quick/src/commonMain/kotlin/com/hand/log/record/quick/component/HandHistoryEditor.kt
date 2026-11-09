package com.hand.log.record.quick.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.hand.log.designsystem.etc.ThemePreview
import com.hand.log.designsystem.etc.ThemePreviews
import com.hand.log.designsystem.theme.HandyTheme
import com.hand.log.ui.poker.CardSize
import com.hand.log.ui.poker.PlayingCard
import com.hand.log.ui.poker.iconRes
import com.hand.log.domain.model.Suit
import org.jetbrains.compose.resources.painterResource
import com.hand.log.record.quick.model.EditorLine
import com.hand.log.record.quick.model.EditorLineKind

@Composable
internal fun HandHistoryEditor(
	lines: List<EditorLine>,
	modifier: Modifier = Modifier,
) {
	val scrollState = rememberScrollState()
	LaunchedEffect(lines) {
		scrollState.animateScrollTo(scrollState.maxValue)
	}
	Column(
		modifier = modifier
			.fillMaxWidth()
			.background(HandyTheme.colorScheme.card, RoundedCornerShape(12.dp))
			.border(1.dp, HandyTheme.colorScheme.border, RoundedCornerShape(12.dp))
			.verticalScroll(scrollState)
			.padding(horizontal = 14.dp, vertical = 12.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		lines.forEach { line ->
			EditorLineRow(line)
		}
	}
}

@Composable
private fun EditorLineRow(line: EditorLine) {
	if (line.kind == EditorLineKind.BOARD) {
		BoardLineRow(line)
		return
	}
	val colors = HandyTheme.colorScheme
	val style = if (line.kind == EditorLineKind.HEADER) HandyTheme.typography.bold12 else HandyTheme.typography.medium14
	val baseColor = when (line.kind) {
		EditorLineKind.HEADER, EditorLineKind.BOARD -> colors.textSecondary
		EditorLineKind.HERO -> colors.gold
		EditorLineKind.AGGRESSIVE -> colors.accent
		EditorLineKind.AUTO -> colors.textSecondary
		EditorLineKind.NORMAL -> colors.textPrimary
	}
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		if (line.text.isNotEmpty()) {
			SuitText(text = line.text, style = style, color = baseColor)
		}
		if (line.cards.isNotEmpty()) {
			Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
				line.cards.forEach { card ->
					PlayingCard(card = card, size = CardSize.XS)
				}
			}
		}
		if (line.afterCards.isNotEmpty()) {
			SuitText(text = line.afterCards.trim(), style = style, color = baseColor)
		}
		DraftAndCaret(line, style)
	}
}

@Composable
private fun BoardLineRow(line: EditorLine) {
	val colors = HandyTheme.colorScheme
	Row(
		modifier = Modifier
			.padding(top = 4.dp)
			.background(colors.muted, RoundedCornerShape(8.dp))
			.border(1.dp, colors.border, RoundedCornerShape(8.dp))
			.padding(horizontal = 8.dp, vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = line.text,
			style = HandyTheme.typography.bold12,
			color = colors.textSecondary,
		)
		if (line.cards.isNotEmpty()) {
			Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
				line.cards.forEach { card ->
					PlayingCard(card = card, size = CardSize.XS)
				}
			}
		}
		DraftAndCaret(line, HandyTheme.typography.bold12)
	}
}

@Composable
private fun DraftAndCaret(line: EditorLine, style: TextStyle) {
	val colors = HandyTheme.colorScheme
	if (line.draft.isNotEmpty()) {
		SuitText(text = line.draft, style = HandyTheme.typography.medium14, color = colors.textPrimary)
	}
	line.caretTrailing?.let { trailing ->
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(width = 2.dp, height = 16.dp)
					.background(colors.primary),
			)
			if (trailing.isNotEmpty()) {
				Text(
					text = trailing,
					style = style,
					color = colors.textSecondary,
				)
			}
		}
	}
}

@Composable
private fun SuitText(text: String, style: TextStyle, color: Color) {
	val colors = HandyTheme.colorScheme
	val inlineContent = remember(style, color, colors) {
		Suit.entries.associate { suit ->
			suit.symbol to InlineTextContent(
				Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.TextCenter),
			) {
				Icon(
					painter = painterResource(suit.iconRes()),
					contentDescription = suit.symbol,
					tint = if (suit.isRed) colors.suitRed else color,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
	}
	Text(
		text = text.withSuitIcons(),
		style = style,
		color = color,
		inlineContent = inlineContent,
	)
}

private fun String.withSuitIcons(): AnnotatedString = buildAnnotatedString {
	val symbols = Suit.entries.map { it.symbol }
	this@withSuitIcons.forEach { char ->
		val symbol = char.toString()
		if (symbol in symbols) appendInlineContent(symbol, symbol) else append(char)
	}
}

@ThemePreviews
@Composable
private fun HandHistoryEditorActionPreview() {
	ThemePreview {
		HandHistoryEditor(
			lines = QuickRecordPreviewData.state(QuickRecordPreviewData.amountInput).lines,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun HandHistoryEditorBoardPreview() {
	ThemePreview {
		HandHistoryEditor(
			lines = QuickRecordPreviewData.state(QuickRecordPreviewData.boardInput).lines,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun HandHistoryEditorDonePreview() {
	ThemePreview {
		HandHistoryEditor(
			lines = QuickRecordPreviewData.state(QuickRecordPreviewData.doneInput).lines,
			modifier = Modifier.padding(16.dp),
		)
	}
}

@ThemePreviews
@Composable
private fun HandHistoryEditorDraftPreview() {
	ThemePreview {
		HandHistoryEditor(
			lines = QuickRecordPreviewData.draftState.lines,
			modifier = Modifier.padding(16.dp),
		)
	}
}
