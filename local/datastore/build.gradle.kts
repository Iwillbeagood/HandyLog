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
			implementation(libs.okio)
			implementation(libs.androidx.datastore.core.okio)
			implementation(libs.kotlinx.serialization.core)
		}
	}
}

wire {
	kotlin {}
	sourcePath {
		srcDir("src/commonMain/proto")
	}
}
