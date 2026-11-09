plugins {
	alias(libs.plugins.base.feature)
}

android.namespace = "com.hand.log.feature.preflop.quiz.common"

kotlin {
	sourceSets {
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
	}
}
