plugins {
	alias(libs.plugins.base.feature)
}

android {
	namespace = "com.hand.log.feature.players"
}

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(project(":feature:players:players-edit"))
		}
	}
}
