package co.touchlab.skie.plugin.fatframework

import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.shim.FrameworkShim
import co.touchlab.skie.plugin.util.InjectedFileSystemOperations
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.newInstance
import co.touchlab.skie.util.cache.copyFileToIfDifferent
import co.touchlab.skie.util.directory.FrameworkLayout
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

object FatFrameworkConfigurator {

    fun configureSkieForFatFrameworks(project: Project) {
        // There is no better way to ensure the configureEach is called only after the configuration done by the Kotlin compiler
        project.gradle.taskGraph.whenReady {
            fixFatFrameworkNameForCocoaPodsPlugin(project)
            configureFatFrameworkPatching(project)
        }
    }

    private fun fixFatFrameworkNameForCocoaPodsPlugin(project: Project) {
        project.pluginManager.withPlugin("kotlin-native-cocoapods") {
            project.kgpShim.configureEachFatFrameworkTask {
                if (task.name == "fatFramework") {
                    baseName = frameworks.map { it.name }.distinct().singleOrNull() ?: return@configureEachFatFrameworkTask
                }
            }
        }
    }

    private fun configureFatFrameworkPatching(project: Project) {
        val injectedFileSystemOperations = project.objects.newInstance<InjectedFileSystemOperations>()

        project.kgpShim.configureEachFatFrameworkTask {
            val serializableFrameworks = frameworks.map { it.toSerializable() }

            task.configureCopySkieFilesToFatFrameworkAction(targetFrameworkLayout, serializableFrameworks, injectedFileSystemOperations)
        }
    }

    private fun Task.configureCopySkieFilesToFatFrameworkAction(
        targetFrameworkLayout: FrameworkLayout,
        serializableFrameworks: List<FrameworkShim.Serializable>,
        injectedFileSystemOperations: InjectedFileSystemOperations,
    ) {
        doLastOptimized {
            copySkieFilesToFatFramework(targetFrameworkLayout, serializableFrameworks, injectedFileSystemOperations)
        }
    }

    private fun copySkieFilesToFatFramework(
        targetFrameworkLayout: FrameworkLayout,
        frameworks: List<FrameworkShim.Serializable>,
        injectedFileSystemOperations: InjectedFileSystemOperations,
    ) {
        copySwiftModulemap(targetFrameworkLayout, frameworks)
        copySwiftHeader(targetFrameworkLayout, frameworks)

        frameworks.forEach { framework ->
            copyFrameworkSpecificFiles(injectedFileSystemOperations, targetFrameworkLayout, framework)
        }
    }

    private fun copySwiftModulemap(
        targetFrameworkLayout: FrameworkLayout,
        frameworks: List<FrameworkShim.Serializable>,
    ) {
        // There shouldn't be any real difference between the frameworks except for architecture, so it's possible to use just the first one
        frameworks.firstOrNull()?.layout?.modulemapFile?.copyFileToIfDifferent(targetFrameworkLayout.modulemapFile)
    }

    private fun copySwiftHeader(
        targetFrameworkLayout: FrameworkLayout,
        frameworks: List<FrameworkShim.Serializable>,
    ) {
        val frameworkWithSwiftHeaderContent = frameworks.map { it to it.layout.swiftHeader.readText() }

        val needsDifferentHeaders = frameworkWithSwiftHeaderContent.distinctBy { it.second }.size > 1

        if (needsDifferentHeaders) {
            createMultiArchitectureSwiftHeader(targetFrameworkLayout, frameworkWithSwiftHeaderContent)
        } else {
            frameworks.firstOrNull()?.layout?.swiftHeader?.copyFileToIfDifferent(targetFrameworkLayout.swiftHeader)
        }
    }

    private fun createMultiArchitectureSwiftHeader(
        targetFrameworkLayout: FrameworkLayout,
        frameworkWithSwiftHeaderContent: List<Pair<FrameworkShim.Serializable, String>>,
    ) {
        targetFrameworkLayout.swiftHeader.writer().use { writer ->
            frameworkWithSwiftHeaderContent.forEachIndexed { i, (framework, swiftHeaderContent) ->
                if (i == 0) {
                    writer.appendLine("#if defined(${framework.architectureClangMacro})\n")
                } else {
                    writer.appendLine("#elif defined(${framework.architectureClangMacro})\n")
                }

                writer.appendLine(swiftHeaderContent)
            }

            writer.appendLine(
                """
                    #else
                    #error Unsupported platform
                    #endif
                    """.trimIndent(),
            )
        }
    }

    private fun copyFrameworkSpecificFiles(
        injectedFileSystemOperations: InjectedFileSystemOperations,
        targetFrameworkLayout: FrameworkLayout,
        framework: FrameworkShim.Serializable,
    ) {
        injectedFileSystemOperations.fileSystemOperations.copy {
            from(framework.layout.apiNotes)
            into(targetFrameworkLayout.headersDir)
        }

        framework.swiftModuleFilesForFatFramework.forEach { swiftmoduleFile ->
            injectedFileSystemOperations.fileSystemOperations.copy {
                from(swiftmoduleFile)
                into(targetFrameworkLayout.swiftModuleParent)
            }
        }
    }

    private val FrameworkShim.Serializable.swiftModuleFilesForFatFramework: List<File>
        get() = listOf(
            layout.abiJson(targetTriple),
            layout.swiftDoc(targetTriple),
            layout.swiftInterface(targetTriple),
            layout.swiftModule(targetTriple),
            layout.swiftSourceInfo(targetTriple),
        )
}
