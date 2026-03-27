plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.wire)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.local.datastore"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.koin.core)

			implementation(libs.androidx.datastore)
			implementation(libs.androidx.datastore.preferences)
			implementation(libs.kotlinx.serialization.core)
			implementation(projects.data.datasource)
		}
	}
}

wire {
	kotlin {}
	sourcePath {
		srcDir("src/commonMain/proto")
	}
}
