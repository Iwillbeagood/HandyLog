plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.preflop.drill"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.preflop.chart)
		}
	}
}
