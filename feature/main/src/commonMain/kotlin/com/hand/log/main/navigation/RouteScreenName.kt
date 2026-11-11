package com.hand.log.main.navigation

import com.hand.log.navigation.navigation.BetSizeSettings
import com.hand.log.navigation.navigation.Contact
import com.hand.log.navigation.navigation.HandDetail
import com.hand.log.navigation.navigation.Legal
import com.hand.log.navigation.navigation.MainTabRoute
import com.hand.log.navigation.navigation.PlayerHands
import com.hand.log.navigation.navigation.PreflopChart
import com.hand.log.navigation.navigation.PreflopChartDrill
import com.hand.log.navigation.navigation.PreflopChartTable
import com.hand.log.navigation.navigation.PreflopQuiz
import com.hand.log.navigation.navigation.PreflopQuizSession
import com.hand.log.navigation.navigation.ProUpgrade
import com.hand.log.navigation.navigation.QuickRecordHand
import com.hand.log.navigation.navigation.RecordHand
import com.hand.log.navigation.navigation.Route
import com.hand.log.navigation.navigation.Table

internal val Route.screenName: String
	get() = when (this) {
		MainTabRoute.Home -> "tab_home"
		is MainTabRoute.Players -> "tab_players"
		MainTabRoute.Preflop -> "tab_preflop"
		MainTabRoute.Settings -> "tab_settings"
		is Table -> "table"
		is RecordHand -> "record_hand"
		is QuickRecordHand -> "quick_record_hand"
		is HandDetail -> "hand_detail"
		is PlayerHands -> "player_hands"
		is PreflopChart -> "preflop_chart"
		is PreflopChartTable -> "preflop_chart_table"
		PreflopChartDrill -> "preflop_chart_drill"
		PreflopQuiz -> "preflop_quiz"
		is PreflopQuizSession -> "preflop_quiz_session"
		BetSizeSettings -> "bet_size_settings"
		ProUpgrade -> "pro_upgrade"
		Contact -> "contact"
		Legal -> "legal"
		else -> "unknown"
	}
