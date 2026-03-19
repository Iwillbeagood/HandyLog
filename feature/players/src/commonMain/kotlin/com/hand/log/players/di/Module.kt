package com.hand.log.players.di

import com.hand.log.players.PlayersViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePlayersModule = module {
	viewModelOf(::PlayersViewModel)
}
