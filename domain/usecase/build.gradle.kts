plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.domain.usecase"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.core.common)
			implementation(projects.domain.repository)
			implementation(libs.kotlinx.coroutines.core)
		}
	}
}
