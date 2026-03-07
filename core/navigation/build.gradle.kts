plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.core.navigation"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.model)

			api(libs.androidx.navigation3.runtime)
			api(libs.androidx.navigation3.ui)
			api(libs.androidx.navigation3.serialization)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.androidx.lifecycle.viewmodel.navigation3)
		}
	}
}
