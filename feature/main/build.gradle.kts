plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.main"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.home)
			implementation(projects.feature.players)
			implementation(projects.feature.table)
			implementation(projects.feature.record)
			implementation(projects.feature.handDetail)
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
