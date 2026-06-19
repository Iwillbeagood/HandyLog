import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.base.kotlin.multiplatform)
	alias(libs.plugins.buildkonfig)
}

val notionToken = Properties().apply {
	val localFile = rootProject.file("local.properties")
	if (localFile.exists()) {
		localFile.inputStream().use(::load)
	}
}.getProperty("notion.token").orEmpty().trim()

android {
	namespace = "com.hand.log.core.common"
}

kotlin {
	sourceSets {
		commonMain.dependencies {
		}
	}
}

buildkonfig {
	packageName = "com.hand.log.common"
	defaultConfigs {
		buildConfigField(STRING, "NOTION_TOKEN", notionToken)
	}
}
