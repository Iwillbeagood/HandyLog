package com.hand.log.convention

import com.hand.log.libs
import com.hand.log.primitive.DetektPlugin
import com.hand.log.primitive.KotlinMultiPlatformAndroidPlugin
import com.hand.log.primitive.KotlinMultiPlatformPlugin
import com.hand.log.primitive.KotlinMultiPlatformiOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KotlinMultiPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("kotlin.multiplatform").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()
        apply<DetektPlugin>()
    }
}
