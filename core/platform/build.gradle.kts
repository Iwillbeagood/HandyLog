plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
}

android.namespace = "com.hand.log.core.platform"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.model)
		}
		androidMain.dependencies {
			implementation(libs.androidx.core.ktx)
			implementation(libs.androidx.activity.compose)
			implementation(libs.firebase.crashlytics)
		}
	}
}
