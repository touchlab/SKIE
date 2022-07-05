package co.touchlab.swiftpack.plugin

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

object SwiftPack {
    val Framework.capitalizedTargetName: String
        get() = target.targetName.capitalized()

    val Framework.unpackSwiftPack: Provider<Sync>
        get() = target.project.tasks.named<Sync>("unpackSwiftPack$capitalizedTargetName")

    val KotlinTarget.mainCompilation: KotlinCompilation<*>
        get() = compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

    fun Project.swiftTemplateDirectory(target: KotlinTarget): Provider<Directory>
        = layout.buildDirectory.dir("generated/swiftpack/${target.targetName}")

    val KotlinCompilation<*>.pluginConfigurationName
        get() = listOfNotNull(PLUGIN_CLASSPATH_CONFIGURATION_NAME, target.disambiguationClassifier, compilationName)
            .withIndex()
            .joinToString("") { (index, value) ->
                if (index == 0) value else value.capitalized()
            }
}