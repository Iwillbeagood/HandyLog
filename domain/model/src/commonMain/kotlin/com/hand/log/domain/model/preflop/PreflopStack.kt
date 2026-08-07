package com.hand.log.domain.model.preflop

/**
 * 차트가 제공되는 유효 스택. [label] 은 셀렉터 표시용이자 원본 JSON 의 stack_size 매칭 키다
 * (대소문자 무시로 매칭 — 생성 데이터의 casing 차이로 스택 전체가 조용히 누락되지 않도록).
 */
enum class PreflopStack(val label: String) {
	BB15("15BB"),
	BB25("25BB"),
	BB40("40BB"),
	BB75("75BB"),
	BB100("100BB"),
	ONLINE("Online"),
	;

	companion object {
		/** 무료로 제공되는 스택 뎁스. 나머지 스택과 프리플랍 퀴즈는 Pro 기능이다. */
		val FREE = BB100

		fun fromLabel(label: String): PreflopStack? = entries.find {
			it.label.equals(label, ignoreCase = true)
		}
	}
}
