package com.hand.log.settings.contact.di

import com.hand.log.domain.usecase.SubmitFeedbackUseCase
import com.hand.log.settings.contact.ContactViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSettingsContactModule = module {
	singleOf(::SubmitFeedbackUseCase)
	viewModelOf(::ContactViewModel)
}
