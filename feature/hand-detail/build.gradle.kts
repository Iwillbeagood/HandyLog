plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.handdetail"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(project(":feature:players:players-edit"))
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
