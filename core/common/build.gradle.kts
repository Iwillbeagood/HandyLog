plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.core.common"

kotlin {
	sourceSets {
		commonMain.dependencies {
		}
	}
}
