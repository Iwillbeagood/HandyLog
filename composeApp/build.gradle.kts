import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.hotReload)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
}

kotlin {
	targets
		.filterIsInstance<KotlinNativeTarget>()
		.forEach { target ->
			target.binaries {
				framework {
					baseName = "ComposeApp"
					isStatic = true
				}
			}
		}

	sourceSets {
		androidMain.dependencies {
			implementation(compose.preview)
			implementation(libs.androidx.activity.compose)
		}
		commonMain.dependencies {
			implementation(projects.feature.main)
			implementation(projects.feature.home)
			implementation(projects.feature.table)
			implementation(projects.feature.record)
			implementation(projects.feature.handDetail)
			implementation(projects.feature.players)
			implementation(projects.domain.model)
			implementation(projects.domain.repository)
			implementation(projects.data.datasource)
			implementation(projects.data.repositoryImpl)
			implementation(projects.local.database)
			implementation(projects.core.designsystem)
			implementation(projects.core.navigation)
			implementation(projects.core.utils)

			implementation(libs.androidx.lifecycle.runtime.compose)

			implementation(libs.koin.compose.viewmodel.navigation)
		}
	}
}

composeCompiler {
	featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

android {
	namespace = "com.hand.log"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.hand.log"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}
}

dependencies {
	debugImplementation(compose.uiTooling)
}
