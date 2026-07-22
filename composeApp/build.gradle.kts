import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.hotReload)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.base.compose.multiplatform)
	alias(libs.plugins.google.services)
	alias(libs.plugins.firebase.perf.plugin)
	alias(libs.plugins.firebase.crashlytics.plugin)
}

kotlin {
	targets
		.filterIsInstance<KotlinNativeTarget>()
		.forEach { target ->
			target.binaries {
				framework {
					baseName = "ComposeApp"
					isStatic = true
					binaryOption("bundleId", "com.hand.log")
				}
			}
		}

	sourceSets {
		androidMain.dependencies {
			implementation(libs.androidx.activity.compose)
			implementation(projects.local.datastore)
			implementation(libs.firebase.perf)
			implementation(libs.firebase.crashlytics)
		}
		commonMain.dependencies {
			implementation(projects.core.common)
			implementation(projects.feature.main)
			implementation(projects.feature.home)
			implementation(projects.feature.table.home)
			implementation(projects.feature.table.tableEdit)
			implementation(projects.feature.table.playerSetup)
			implementation(projects.feature.record)
			implementation(projects.feature.handDetail)
			implementation(projects.feature.players.home)
			implementation(projects.feature.players.hands)
			implementation(projects.feature.settings.home)
			implementation(projects.feature.settings.betsize)
			implementation(projects.feature.settings.contact)
			implementation(projects.domain.model)
			implementation(projects.domain.repository)
			implementation(projects.data.datasource)
			implementation(projects.data.repositoryImpl)
			implementation(projects.local.database)
			implementation(projects.local.datastore)
			implementation(projects.core.designsystem)
			implementation(projects.core.navigation)
			implementation(projects.core.platform)
			implementation(projects.core.utils)

			implementation(libs.androidx.lifecycle.runtime.compose)

			implementation(libs.koin.compose.viewmodel.navigation)
		}
	}
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
	buildFeatures {
		buildConfig = true
	}
	flavorDimensions += "tier"
	productFlavors {
		create("free") {
			dimension = "tier"
			applicationId = "com.hand.log"
			buildConfigField("Boolean", "IS_PRO", "false")
		}
		create("paid") {
			dimension = "tier"
			applicationId = "com.hand.log.pro"
			buildConfigField("Boolean", "IS_PRO", "true")
		}
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

afterEvaluate {
	tasks.matching { it.name.contains("Paid") && it.name.contains("GoogleServices") }.configureEach {
		enabled = false
	}
	tasks.matching { it.name.contains("Paid") && it.name.contains("Crashlytics") }.configureEach {
		enabled = false
	}
	tasks.matching { it.name.contains("Paid") && it.name.contains("FirebasePerf") }.configureEach {
		enabled = false
	}
}

dependencies {
	debugImplementation(compose.uiTooling)
}
