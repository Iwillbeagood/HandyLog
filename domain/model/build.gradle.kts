plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.domain.model"

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(libs.kotlinx.datetime)
			implementation(libs.kotlinx.serialization.core)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
