plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.record"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.datetime)
			implementation(projects.feature.table.tableEdit)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
