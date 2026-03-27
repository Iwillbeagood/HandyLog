plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.data.datasource"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.model)
			implementation(projects.domain.repository)
			implementation(projects.core.utils)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.koin.core)
		}
	}
}
