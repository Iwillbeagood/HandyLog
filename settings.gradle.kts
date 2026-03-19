pluginManagement {
    includeBuild("build-logic")
    repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}

rootProject.name = "HandyLog"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")

// data
include(
	":data:datasource",
	":data:repositoryImpl",
)

// local
include(
	":local:datastore",
	":local:database",
)

// domain
include(
	":domain:model",
	":domain:repository",
	":domain:usecase",
)

// core
include(
    ":core:common",
    ":core:designsystem",
    ":core:navigation",
    ":core:res",
    ":core:ui",
    ":core:utils",
)

// feature
include(
    ":feature:main",
    ":feature:home",
    ":feature:players",
    ":feature:record",
    ":feature:table",
    ":feature:hand-detail",
)
