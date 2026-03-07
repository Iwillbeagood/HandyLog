plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.remote.network"

kotlin {
	sourceSets {
		androidMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		commonMain.dependencies {
			implementation(projects.domain.repository)
			implementation(projects.data.datasource)

			implementation(projects.core.utils)
			implementation(projects.core.common)

			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(libs.ktor.logging)

			implementation(libs.koin.core)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}
