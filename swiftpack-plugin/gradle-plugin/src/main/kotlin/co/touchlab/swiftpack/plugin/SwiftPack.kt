package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.spec.NameMangling.demangledClassName
import co.touchlab.swiftpack.spec.SwiftPackModule
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import java.io.File

object SwiftPack {
    val Framework.capitalizedTargetName: String
        get() = target.targetName.capitalized()

    val Framework.unpackSwiftPack: Provider<Sync>
        get() = target.project.tasks.named<Sync>("unpackSwiftPack$capitalizedTargetName")

    val Framework.swiftPackModules: Provider<List<NamespacedSwiftPackModule>>
        get() = unpackSwiftPack.map { it.destinationDir }.zip(project.swiftTemplateDirectory(target)) { dependenciesDir, localDir ->
            fun isSwiftPackModule(file: File): Boolean = file.isFile && file.extension == "swiftpack"
            val dependencyModules = dependenciesDir.listFiles(::isSwiftPackModule)?.map { file ->
                val module = SwiftPackModule.read(file)
                NamespacedSwiftPackModule(
                    file.parent,
                    module,
                )
            }
            val localModules = localDir.asFile.listFiles(::isSwiftPackModule)?.map { file ->
                NamespacedSwiftPackModule(
                    file.parent,
                    SwiftPackModule.read(file),
                )
            }
            listOfNotNull(dependencyModules, localModules).flatten()
        }

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


    fun SwiftPackModule.TemplateFile.produceSwiftFile(swiftNameProvider: SwiftNameProvider): String {
        return contents.replace("KotlinSwiftGen\\.([a-zA-Z0-9_]+)".toRegex()) { match ->
            swiftNameProvider.getSwiftName(match.groupValues[1].demangledClassName)
        }
    }
}
