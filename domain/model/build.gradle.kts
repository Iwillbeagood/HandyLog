plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.domain.model"

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(libs.kotlinx.datetime)
		}
	}
}
