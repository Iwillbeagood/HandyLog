import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

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

// keystore.properties 가 있으면 릴리스 서명 설정을 구성한다(없으면 release 서명 없이 빌드).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
	if (keystorePropertiesFile.exists()) {
		keystorePropertiesFile.inputStream().use { load(it) }
	}
}

android {
	namespace = "com.hand.log"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	signingConfigs {
		if (keystorePropertiesFile.exists()) {
			create("release") {
				storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
				storePassword = keystoreProperties.getProperty("storePassword")
				keyAlias = keystoreProperties.getProperty("keyAlias")
				keyPassword = keystoreProperties.getProperty("keyPassword")
			}
		}
	}

	defaultConfig {
		applicationId = "com.hand.log"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		// CI 에서 -PversionCode 로 주입하면 그 값을, 없으면 로컬 빌드용 기본값 1 을 사용한다.
		versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
		versionName = "1.0.0"
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
			if (keystorePropertiesFile.exists()) {
				signingConfig = signingConfigs.getByName("release")
			}
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
	// paid flavor(com.hand.log.pro)용 google-services.json 이 있으면 Firebase(크래시 리포팅 등)를 그대로 활성화하고,
	// 없으면 관련 태스크만 비활성화해 빌드가 깨지지 않게 한다.
	val paidFirebaseConfigured = file("src/paid/google-services.json").exists()
	if (!paidFirebaseConfigured) {
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
}

dependencies {
	debugImplementation(compose.uiTooling)
}
