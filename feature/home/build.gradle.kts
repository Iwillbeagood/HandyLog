plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.home"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.datetime)
		}
	}
}
