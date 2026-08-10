plugins {
	alias(libs.plugins.base.feature)
	alias(libs.plugins.kotlin.serialization)
}

android.namespace = "com.hand.log.feature.preflop.chart"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.serialization.json)
		}
	}
}
