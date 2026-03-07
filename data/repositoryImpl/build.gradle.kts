plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.data.repositoryimpl"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.model)
			implementation(projects.domain.repository)
			implementation(projects.data.datasource)
		}
	}
}
