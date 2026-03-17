package com.hand.log.primitive

import com.hand.log.library
import com.hand.log.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.composeMultiplatformDependencies() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain {
                dependencies {
                    implementation(libs.library("compose-runtime"))
                    implementation(libs.library("compose-foundation"))
                    implementation(libs.library("compose-material3"))
                    implementation(libs.library("compose-ui"))
                    implementation(libs.library("compose-resources"))
                    implementation(libs.library("compose-ui-tooling-preview"))
                }
            }
        }
    }

    dependencies {
        "debugImplementation"(libs.library("compose-ui-tooling"))
    }
}
