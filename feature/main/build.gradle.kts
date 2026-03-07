plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.main"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.home)
			implementation(libs.kotlinx.immutable)
			implementation(libs.androidx.lifecycle.viewmodel.navigation3)
		}

		appleMain {
			dependsOn(commonMain.get())
		}

		androidMain {
			dependsOn(commonMain.get())
		}
	}
}
