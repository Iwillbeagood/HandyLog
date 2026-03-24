package com.hand.log.record.contract

import androidx.compose.runtime.Composable
import handylog.core.res.generated.resources.Res
import handylog.core.res.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecordStep.localizedLabel(): String = when (this) {
	RecordStep.SETUP -> stringResource(Res.string.step_setup)
	RecordStep.PREFLOP -> stringResource(Res.string.step_preflop)
	RecordStep.FLOP -> stringResource(Res.string.step_flop)
	RecordStep.TURN -> stringResource(Res.string.step_turn)
	RecordStep.RIVER -> stringResource(Res.string.step_river)
	RecordStep.SHOWDOWN -> stringResource(Res.string.step_showdown)
}
