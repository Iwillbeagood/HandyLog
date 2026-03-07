plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
}

android.namespace = "com.hand.log.core.ui"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(compose.foundation)
			implementation(compose.ui)
		}
	}
}
