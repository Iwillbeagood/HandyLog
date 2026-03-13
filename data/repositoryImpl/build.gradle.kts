plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.data.repositoryimpl"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.repository)
			implementation(projects.data.datasource)
			implementation(projects.core.utils)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.koin.core)
		}
	}
}
