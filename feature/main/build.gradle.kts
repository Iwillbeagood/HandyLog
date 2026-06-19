plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.main"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.home)
			implementation(projects.feature.players.home)
			implementation(projects.feature.players.hands)
			implementation(projects.feature.table.home)
			implementation(projects.feature.record)
			implementation(projects.feature.handDetail)
			implementation(projects.feature.settings.home)
			implementation(projects.feature.settings.betsize)
			implementation(projects.feature.settings.upgrade)
			implementation(projects.feature.settings.contact)
			implementation(libs.kotlinx.immutable)
			implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
		}

	}
}
