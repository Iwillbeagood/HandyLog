plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
}

android.namespace = "com.hand.log.core.designsystem"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.core.res)
		}

		appleMain {
			dependencies {
				implementation(libs.ktor.client.darwin)
			}
		}
		androidMain {
			dependencies {
				implementation(libs.ktor.client.okhttp)
			}
		}
		jvmMain {
			dependencies {
				implementation(libs.ktor.client.okhttp)
			}
		}
	}
}

android.namespace = "com.droidknights.app.core.designsystem"
