package co.touchlab.skie.gradle.publish

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

fun Project.publishCode() {
    plugins.withType(KotlinPluginWrapper::class.java).configureEach {
        extensions.configure(JavaPluginExtension::class.java) {
            withJavadocJar()
            withSourcesJar()
        }
    }
}
