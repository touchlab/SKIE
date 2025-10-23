package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

// WIP
fun Project.publishCode() {
    plugins.withType(KotlinPluginWrapper::class.java).configureEach {
        extensions.configure(JavaPluginExtension::class.java) {
            withJavadocJar()
            withSourcesJar()
        }
    }
}
