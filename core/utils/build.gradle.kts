plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
}

android.namespace = "com.hand.log.core.utils"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.core.res)
			implementation(projects.domain.model)
		}
		androidMain.dependencies {
			implementation(libs.androidx.core.ktx)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
