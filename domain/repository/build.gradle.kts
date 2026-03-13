plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.domain.repository"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.koin.core)
			implementation(libs.kotlinx.coroutines.core)

			api(projects.domain.model)
			implementation(projects.core.utils)
		}
	}
}
