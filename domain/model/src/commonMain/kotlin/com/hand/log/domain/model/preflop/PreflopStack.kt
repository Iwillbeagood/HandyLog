package com.hand.log.domain.model.preflop

/** 차트가 제공되는 유효 스택 깊이(빅블라인드). */
enum class PreflopStack(val label: String) {
	BB15("15BB"),
	BB25("25BB"),
	BB40("40BB"),
	BB75("75BB"),
	BB100("100BB"),
	;

	companion object {
		fun fromLabel(label: String): PreflopStack? = entries.find { it.label == label }
	}
}
