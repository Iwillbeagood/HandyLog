plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.main"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.home)
			implementation(projects.feature.players)
			implementation(projects.feature.table.home)
			implementation(projects.feature.record)
			implementation(projects.feature.handDetail)
			implementation(projects.feature.settings.home)
			implementation(projects.feature.settings.betsize)
			implementation(libs.kotlinx.immutable)
			implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
		}

		appleMain {
			dependsOn(commonMain.get())
		}

		androidMain {
			dependsOn(commonMain.get())
		}
	}
}
