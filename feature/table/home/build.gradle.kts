plugins {
	alias(libs.plugins.base.feature)
}

android {
	namespace = "com.hand.log.feature.table"
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(libs.kotlinx.datetime)
			implementation(projects.feature.table.playerSetup)
			implementation(projects.feature.table.tableEdit)
		}
	}
}
