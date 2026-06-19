plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.data.datasource"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.domain.model)
			implementation(projects.domain.repository)
			implementation(projects.core.common)
			implementation(projects.core.utils)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.koin.core)

			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(libs.ktor.logging)
		}
		androidMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}
