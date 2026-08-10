package com.hand.log.preflop.quiz.common

import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.quiz_type_mixed_title
import handylog.core.res.generated.resources.quiz_type_rfi_title
import handylog.core.res.generated.resources.quiz_type_vs3bet_title
import handylog.core.res.generated.resources.quiz_type_vsopen_title
import org.jetbrains.compose.resources.StringResource

/** 퀴즈 유형 — 차트 시나리오(RFI/FACING_RFI/VS_3BET)에 1:1 대응하고, [MIXED] 는 셋을 섞어 출제한다. */
enum class PreflopQuizType {
	RFI,
	VS_OPEN,
	VS_3BET,
	MIXED,
}

/** 퀴즈 유형의 표시용 제목 리소스(오픈 레이즈·vs 오픈·vs 3벳·종합). */
fun PreflopQuizType.titleRes(): StringResource = when (this) {
	PreflopQuizType.RFI -> Res.string.quiz_type_rfi_title
	PreflopQuizType.VS_OPEN -> Res.string.quiz_type_vsopen_title
	PreflopQuizType.VS_3BET -> Res.string.quiz_type_vs3bet_title
	PreflopQuizType.MIXED -> Res.string.quiz_type_mixed_title
}
