plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.preflop.quiz.home"

kotlin {
	sourceSets {
		commonMain.dependencies {
			implementation(projects.feature.preflop.quiz.common)
			implementation(projects.feature.preflop.chart)
		}
	}
}
