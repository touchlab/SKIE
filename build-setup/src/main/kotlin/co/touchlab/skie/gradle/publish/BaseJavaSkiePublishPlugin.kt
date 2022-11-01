package co.touchlab.skie.gradle.publish

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class BaseJavaSkiePublishPlugin : BaseSkiePublishPlugin() {

    override fun apply(target: Project) {
        super.apply(target)

        target.plugins.withType(KotlinPluginWrapper::class.java) {
            afterKotlinPlugin(target)
        }
    }

    protected open fun afterKotlinPlugin(target: Project) {
        configurePublicationContent(target)
    }

    private fun configurePublicationContent(target: Project) {
        target.extensions.configure(JavaPluginExtension::class.java) {
            withJavadocJar()
            withSourcesJar()
        }
    }
}
