plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
}

android.namespace = "com.hand.log.core.utils"

dependencies {
	implementation(projects.core.res)
}
