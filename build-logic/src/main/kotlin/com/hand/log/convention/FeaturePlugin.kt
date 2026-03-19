package com.hand.log.convention

import com.hand.log.library
import com.hand.log.libs
import com.hand.log.primitive.DetektPlugin
import com.hand.log.primitive.KotlinMultiPlatformAndroidPlugin
import com.hand.log.primitive.KotlinMultiPlatformPlugin
import com.hand.log.primitive.KotlinMultiPlatformiOSPlugin
import com.hand.log.primitive.composeMultiplatformDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("android.library").get().get().pluginId)
            apply(libs.findPlugin("kotlin.multiplatform").get().get().pluginId)
            apply(libs.findPlugin("compose.multiplatform").get().get().pluginId)
            apply(libs.findPlugin("compose.compiler").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()
        apply<DetektPlugin>()

        composeMultiplatformDependencies()

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(project(":core:designsystem"))
                        implementation(project(":core:ui"))
                        implementation(project(":core:navigation"))
                        implementation(project(":core:res"))
                        implementation(project(":core:utils"))
                        implementation(project(":domain:model"))
                        implementation(project(":domain:usecase"))
                        implementation(project(":domain:repository"))
                        implementation(libs.library("androidx.lifecycle.runtime.compose"))
                        implementation(libs.library("koin.compose.viewmodel.navigation"))
                        implementation(libs.library("compose.navigationevent"))
                    }
                }

            }
        }
    }
}
