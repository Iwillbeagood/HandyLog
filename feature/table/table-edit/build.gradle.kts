plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.table.tableedit"

kotlin {
	sourceSets {
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
