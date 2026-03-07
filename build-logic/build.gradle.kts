plugins {
    `kotlin-dsl`
}

dependencies {
	implementation(libs.android.gradlePlugin)
	implementation(libs.kotlin.gradlePlugin)
	compileOnly(libs.compose.compiler.gradlePlugin)
	compileOnly(libs.compose.gradlePlugin)
	compileOnly(libs.verify.detektPlugin)
}

gradlePlugin {
    plugins {
		register("kmpIos") {
			id = libs.plugins.base.kmp.ios.get().pluginId
			implementationClass = "com.hand.log.primitive.KotlinMultiPlatformiOSPlugin"
		}
		register("kmpAndroid") {
			id = libs.plugins.base.kmp.android.get().pluginId
			implementationClass = "com.hand.log.primitive.KotlinMultiPlatformAndroidPlugin"
		}
		register("kmpPrimitive") {
			id = libs.plugins.base.kmp.primitive.get().pluginId
			implementationClass = "com.hand.log.primitive.KotlinMultiPlatformPlugin"
		}
		register("kmpConvention") {
			id = libs.plugins.base.kotlin.multiplatform.get().pluginId
			implementationClass = "com.hand.log.convention.KotlinMultiPlatformConventionPlugin"
		}
		register("cmpConvention") {
			id = libs.plugins.base.compose.multiplatform.get().pluginId
			implementationClass = "com.hand.log.convention.ComposeMultiPlatformConventionPlugin"
		}
		register("feature") {
			id = libs.plugins.base.feature.get().pluginId
			implementationClass = "com.hand.log.convention.FeaturePlugin"
		}
		register("detekt") {
			id = libs.plugins.base.verify.detekt.get().pluginId
			implementationClass = "com.hand.log.primitive.DetektPlugin"
		}
	}
}
