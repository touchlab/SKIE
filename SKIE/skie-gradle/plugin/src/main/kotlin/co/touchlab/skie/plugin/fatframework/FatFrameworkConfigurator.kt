package co.touchlab.skie.plugin.fatframework

import co.touchlab.skie.plugin.util.InjectedFileSystemOperations
import co.touchlab.skie.plugin.util.TargetTriple
import co.touchlab.skie.plugin.util.darwinTarget
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.newInstance
import co.touchlab.skie.plugin.util.withType
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkLayout
import org.jetbrains.kotlin.konan.target.Architecture
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
            project.tasks.withType<FatFrameworkTask>().matching { it.name == "fatFramework" }.configureEach {
                baseName = frameworks.map { it.name }.distinct().singleOrNull() ?: return@configureEach
            }
        }
    }

    private fun configureFatFrameworkPatching(project: Project) {
        val injectedFileSystemOperations = project.objects.newInstance<InjectedFileSystemOperations>()

        project.tasks.withType<FatFrameworkTask>().configureEach {
            val primaryFramework = frameworks.firstOrNull() ?: return@configureEach

            // There shouldn't be any real difference between the frameworks except for architecture, so we consider the first one "primary"
            val target = FrameworkLayout(
                rootDir = fatFramework,
                isMacosFramework = primaryFramework.files.isMacosFramework,
            )

            val targetModuleFile = target.moduleFile
            val targetHeaderDir = target.headerDir
            val targetSwiftHeader = target.swiftHeader
            val targetSwiftModuleDir = target.swiftModuleDir
            val primaryFrameworkModuleFile = primaryFramework.files.moduleFile

            val frameworkByArchitecture = frameworks.associateBy { it.target.architecture }
            val swiftHeadersWithArchitectureClangMacro = frameworkByArchitecture.map { it.value.files.swiftHeader to it.key.clangMacro }

            val frameworksFiles = frameworks.map {
                FrameworkFiles(it.files.swiftModuleDir, it.files.apiNotes, it.files.swiftModuleFiles(it.darwinTarget.targetTriple))
            }

            doLastOptimized {
                // FatFrameworkTask writes its own
                targetModuleFile.writeText(primaryFrameworkModuleFile.readText())

                targetSwiftHeader.writer().use { writer ->
                    val swiftHeaderContentsWithArchitectureClangMacro = swiftHeadersWithArchitectureClangMacro.map { it.first.readText() to it.second }

                    if (swiftHeaderContentsWithArchitectureClangMacro.distinctBy { it.first }.size == 1) {
                        writer.write(swiftHeaderContentsWithArchitectureClangMacro.first().first)
                    } else {
                        swiftHeaderContentsWithArchitectureClangMacro.forEachIndexed { i, (content, clangMacro) ->
                            if (i == 0) {
                                writer.appendLine("#if defined($clangMacro)\n")
                            } else {
                                writer.appendLine("#elif defined($clangMacro)\n")
                            }
                            writer.appendLine(content)
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

                targetSwiftModuleDir.mkdirs()

                frameworksFiles.forEach { frameworkFiles ->
                    injectedFileSystemOperations.fileSystemOperations.copy {
                        from(frameworkFiles.apiNotes)
                        into(targetHeaderDir)
                    }

                    frameworkFiles.swiftModuleFiles.forEach { swiftmoduleFile ->
                        injectedFileSystemOperations.fileSystemOperations.copy {
                            from(swiftmoduleFile)
                            into(targetSwiftModuleDir)
                        }
                    }
                }
            }
        }
    }

    data class FrameworkFiles(
        val swiftModuleDir: File,
        val apiNotes: File,
        val swiftModuleFiles: List<File>,
    )
}

private val FrameworkLayout.frameworkName: String
    get() = rootDir.nameWithoutExtension

private val FrameworkLayout.swiftHeader: File
    get() = headerDir.resolve("$frameworkName-Swift.h")

private val FrameworkLayout.apiNotes: File
    get() = headerDir.resolve("$frameworkName.apinotes")

private val FrameworkLayout.swiftModuleDir: File
    get() = modulesDir.resolve("$frameworkName.swiftmodule")

private fun FrameworkLayout.swiftModuleFiles(triple: TargetTriple): List<File> {
    return listOf("abi.json", "swiftdoc", "swiftinterface", "swiftmodule", "swiftsourceinfo").map { ext ->
        swiftModuleDir.resolve("$triple.$ext")
    }
}

private val Architecture.clangMacro: String
    get() = when (this) {
        Architecture.X86 -> "__i386__"
        Architecture.X64 -> "__x86_64__"
        Architecture.ARM32 -> "__arm__"
        Architecture.ARM64 -> "__aarch64__"
        else -> error("Fat frameworks are not supported for architecture `$name`")
    }
