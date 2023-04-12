package co.touchlab.skie.gradle.publish

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class BaseJavaSkiePublishPlugin : BaseSkiePublishPlugin() {

    override fun apply(target: Project) {
        super.apply(target)

        target.plugins.withType(KotlinPluginWrapper::class.java).configureEach {
            afterKotlinPlugin(target)
        }
    }

    protected open fun afterKotlinPlugin(target: Project) {
    }
}
