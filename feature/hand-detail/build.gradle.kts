plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.handdetail"

kotlin {
	sourceSets {
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
