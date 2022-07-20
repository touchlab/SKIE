package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.plugin.SwiftPack.unpackSwiftPack
import co.touchlab.swiftpack.plugin.SwiftPackPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkLayout
import org.jetbrains.kotlin.konan.target.Architecture
import java.io.File

const val EXTENSION_NAME = "swiftkt"

// We need to use an anonymous class instead of lambda to keep execution optimizations.
// https://docs.gradle.org/7.4.2/userguide/validation_problems.html#implementation_unknown
@Suppress("ObjectLiteralToLambda")
abstract class SwiftKtPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create(EXTENSION_NAME, SwiftKtExtension::class.java, this)

        // WORKAROUND: Fix fat framework name for CocoaPods plugin.
        pluginManager.withPlugin("kotlin-native-cocoapods") {
            tasks.withType<FatFrameworkTask>().matching { it.name == "fatFramework" }.configureEach { task ->
                // Unfortunately has to be done in `doFirst` to make sure the task is already configured by the plugin when we run our code
                task.doFirst(object: Action<Task> {
                    override fun execute(p0: Task) {
                        val commonFrameworkName = task.frameworks.map { it.name }.distinct().singleOrNull() ?: return
                        task.baseName = commonFrameworkName
                    }
                })
            }
        }

        afterEvaluate {
            extension.isSwiftPackEnabled.finalizeValue()
            if (extension.isSwiftPackEnabled.get()) {
                apply<SwiftPackPlugin>()
            }

            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: return@afterEvaluate
            val appleTargets = kotlin.targets
                .mapNotNull { it as? KotlinNativeTarget }
                .filter { it.konanTarget.family.isAppleFamily }

            appleTargets.forEach { target ->
                val frameworks = target.binaries.mapNotNull { it as? Framework }
                frameworks.forEach { framework ->
                    if (!framework.isStatic) {
                        framework.linkTask.doFirst(object : Action<Task> {
                            override fun execute(t: Task) {
                                framework.isStatic = true
                            }
                        })
                        framework.linkTask.doLast(object : Action<Task> {
                            override fun execute(t: Task) {
                                framework.isStatic = false
                            }
                        })
                    }

                    val swiftPackExpandTask = if (extension.isSwiftPackEnabled.get()) {
                        tasks.register<SwiftPackExpandTask>(framework.swiftPackExpandTaskName, framework).configuring {
                            dependsOn(framework.unpackSwiftPack)
                        }
                    } else {
                        null
                    }

                    val swiftCompileTaskProvider = tasks.register<SwiftCompileTask>(framework.swiftCompileTaskName, framework).configuring {
                        dependsOn(framework.linkTask)

                        val target = framework.target
                        description =
                            "Compiles Swift code for ${framework.outputKind.description} '${framework.name}' for a target '${target.name}'."
                        enabled = framework.linkTask.enabled
                        outputs.upToDateWhen { framework.linkTask.state.upToDate }

                        val defaultSwiftSourceSet = configureSwiftSourceSet(framework.compilation.defaultSourceSet)
                        val allSwiftSourceSets = (framework.compilation.allKotlinSourceSets - framework.compilation.defaultSourceSet)
                            .map { configureSwiftSourceSet(it) } + listOf(defaultSwiftSourceSet)

                        if (swiftPackExpandTask != null) {
                            dependsOn(swiftPackExpandTask)
                            defaultSwiftSourceSet.srcDir(swiftPackExpandTask.map { it.outputDir })
                        }

                        sourceFiles.set(project.objects.fileCollection().from(allSwiftSourceSets))
                        outputDir.set(extension.outputDir.dir(framework.name + File.separator + target.targetName))
                    }
                    framework.linkTask.finalizedBy(swiftCompileTaskProvider)

                    // Make sure linkTask dependencies are executed after swiftCompileTask.
                    project.tasks.configureEach {
                        if (it.dependsOn.contains(framework.linkTask) && it !is SwiftCompileTask) {
                            it.dependsOn(swiftCompileTaskProvider)
                        }
                    }
                }
            }

            tasks.withType<FatFrameworkTask>().configureEach { task ->
                task.doLast(object: Action<Task> {
                    override fun execute(p0: Task) {
                        val target = FrameworkLayout(task.fatFramework)

                        val frameworksByArchs = task.frameworks.associateBy { it.target.architecture }
                        target.swiftHeader.writer().use { writer ->
                            val swiftHeaderContents = frameworksByArchs.mapValues { (_, framework) ->
                                framework.files.swiftHeader.readText()
                            }

                            if (swiftHeaderContents.values.distinct().size == 1) {
                                writer.write(swiftHeaderContents.values.first())
                            } else {
                                swiftHeaderContents.toList().forEachIndexed { i, (arch, content) ->
                                    val macro = arch.clangMacro
                                    if (i == 0) {
                                        writer.appendLine("#if defined($macro)\n")
                                    } else {
                                        writer.appendLine("#elif defined($macro)\n")
                                    }
                                    writer.appendLine(content)
                                }
                                writer.appendLine(
                                    """
                                    #else
                                    #error Unsupported platform
                                    #endif
                                    """.trimIndent()
                                )
                            }
                        }

                        target.swiftModuleDir.mkdirs()

                        frameworksByArchs.toList().forEach { (arch, framework) ->
                            framework.files.swiftModuleFiles(framework.darwinTarget.targetTriple).forEach { swiftmoduleFile ->
                                it.copy {
                                    it.from(swiftmoduleFile)
                                    it.into(target.swiftModuleDir)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun Project.configureSwiftSourceSet(kotlinSourceSet: KotlinSourceSet): SourceDirectorySet {
        val swiftSourceSetName = "${kotlinSourceSet.name} Swift source"

        return objects.sourceDirectorySet(swiftSourceSetName, swiftSourceSetName).apply {
            filter.include("**/*.swift")
            srcDirs(kotlinSourceSet.swiftSourceDirectory)
        }
    }

    private val KotlinSourceSet.swiftSourceDirectory: String
        get() = "src/$name/swift"

    private val Framework.swiftCompileTaskName: String
        get() = listOf("swiftCompile", name.capitalized(), target.targetName.capitalized()).joinToString("")

    private val Framework.swiftPackExpandTaskName: String
        get() = listOf("swiftPackExpand", name.capitalized(), target.targetName.capitalized()).joinToString("")

    private inline fun <T: Task> TaskProvider<T>.configuring(crossinline configuration: T.() -> Unit): TaskProvider<T> {
        configure {
            configuration(it)
        }
        return this
    }
}

private val FrameworkLayout.frameworkName: String
    get() = rootDir.nameWithoutExtension

val FrameworkLayout.swiftHeader: File
    get() = headerDir.resolve("$frameworkName-Swift.h")

val FrameworkLayout.swiftModuleDir: File
    get() = modulesDir.resolve("$frameworkName.swiftmodule")

fun FrameworkLayout.swiftModuleFiles(triple: TargetTriple): List<File> {
    return listOf("abi.json", "swiftdoc", "swiftinterface", "swiftmodule", "swiftsourceinfo").map { ext ->
        swiftModuleDir.resolve("$triple.$ext")
    }
}

val Architecture.clangMacro: String
    get() = when (this) {
        Architecture.X86 -> "__i386__"
        Architecture.X64 -> "__x86_64__"
        Architecture.ARM32 -> "__arm__"
        Architecture.ARM64 -> "__aarch64__"
        else -> error("Fat frameworks are not supported for architecture `$name`")
    }
