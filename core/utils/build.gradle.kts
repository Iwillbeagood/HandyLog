plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.core.utils"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.core.res)
			implementation(projects.domain.model)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
