plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.settings.home"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.core.common)
		}
	}
}
