plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.room)
	alias(libs.plugins.ksp)
}

android.namespace = "com.hand.log.local.database"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.room.runtime)
			implementation(libs.sqlite.bundled)
			implementation(libs.kotlinx.coroutines.core)
			implementation(libs.kotlinx.serialization.core)
			implementation(libs.kotlinx.serialization.json)
			implementation(projects.domain.model)
			implementation(projects.data.datasource)
			implementation(libs.koin.core)
		}
		androidMain.dependencies {
			implementation(libs.koin.android)
		}
	}
}

dependencies {
	add("kspAndroid", libs.room.compiler)
	add("kspIosSimulatorArm64", libs.room.compiler)
	add("kspIosX64", libs.room.compiler)
	add("kspIosArm64", libs.room.compiler)
}

room {
	schemaDirectory("$projectDir/schemas")
}
