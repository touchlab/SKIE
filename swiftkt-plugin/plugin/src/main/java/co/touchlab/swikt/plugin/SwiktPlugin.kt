package co.touchlab.swikt.plugin

import co.touchlab.swiftpack.plugin.SwiftPack
import co.touchlab.swiftpack.plugin.SwiftPack.unpackSwiftPack
import co.touchlab.swiftpack.plugin.SwiftPackPlugin
import co.touchlab.swiftpack.spec.NameMangling.demangledClassName
import co.touchlab.swiftpack.spec.SwiftPackModule
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkLayout
import org.jetbrains.kotlin.konan.target.Architecture
import java.io.File

const val EXTENSION_NAME = "swikt"

abstract class SwiktPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create(EXTENSION_NAME, SwiktExtension::class.java, this)

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

            val appleSourceSets = appleTargets
                .flatMap { it.compilations } // TODO should we filter out test sources?
                .flatMap { it.allKotlinSourceSets }
                .toSet()

            appleSourceSets.forEach { sourceSet ->
                val generateKotlinTask =
                    tasks.register(sourceSet.generateKotlinTaskName, KotlinGenerateTask::class.java, sourceSet)
                generateKotlinTask.configure {
                    it.generatedSourceDir.set(file(sourceSet.generatedKotlinDirectory))
                }
                val generateSwiftTask =
                    tasks.register(sourceSet.generateSwiftTaskName, SwiftGenerateTask::class.java, sourceSet)
                generateSwiftTask.configure {
                    it.generatedSourceDir.set(file(sourceSet.generatedSwiftDirectory))
                }
            }

            appleTargets.forEach { target ->
                val frameworks = target.binaries.mapNotNull { it as? Framework }
                frameworks.forEach { framework ->
                    // We need to use an anonymous class instead of lambda to keep execution optimizations.
                    // https://docs.gradle.org/7.4.2/userguide/validation_problems.html#implementation_unknown
                    @Suppress("ObjectLiteralToLambda")
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

                    val swiftCompileTaskProvider = tasks.register(framework.swiftCompileTaskName, SwiftCompileTask::class.java, framework)
                    swiftCompileTaskProvider.configure {
                        it.dependsOn(framework.linkTask)
                        it.dependsOn(
                            *framework.compilation.allKotlinSourceSets
                                .flatMap { listOf(it.generateKotlinTaskName, it.generateSwiftTaskName) }
                                .toTypedArray()
                        )

                        if (extension.isSwiftPackEnabled.get()) {
                            it.dependsOn(framework.unpackSwiftPack)
                            it.doFirst {
                                framework.unpackSwiftPack.get().destinationDir.listFiles()?.map { file ->
                                    val module = SwiftPackModule.read(file)
                                    module.files.forEach { swiftFileContents ->
                                        val replacedContents = swiftFileContents.replace("KotlinSwiftGen\\.([a-zA-Z0-9_]+)".toRegex()) { match ->
                                            match.groupValues[1].demangledClassName.split(".").last()
                                        }
                                        val targetSwiftFile = projectDir
                                            .resolve(framework.compilation.defaultSourceSet.generatedSwiftDirectory)
                                            .resolve(file.nameWithoutExtension)
                                        targetSwiftFile.writeText(replacedContents)
                                    }
                                }
                            }
                        }

                        val target = framework.target
                        it.description =
                            "Compiles Swift code for ${framework.outputKind.description} '${framework.name}' for a target '${target.name}'."
                        it.enabled = framework.linkTask.enabled
                        it.outputs.upToDateWhen { framework.linkTask.state.upToDate }

                        val swiftSourceSets = framework.compilation.allKotlinSourceSets.map { kotlinSourceSet ->
                            "${kotlinSourceSet.name} Swift source".let {
                                project.objects.sourceDirectorySet(it, it).apply {
                                    filter.include("**/*.swift")
                                    srcDirs(kotlinSourceSet.generatedSwiftDirectory)
                                    srcDirs(kotlinSourceSet.kspSwiftDirectory, kspCommonSwiftDirectory)
                                    srcDirs(kotlinSourceSet.swiftSourceDirectory)
                                }
                            }
                        }
                        framework.compilation.allKotlinSourceSets.forEach {
                            it.kotlin.srcDirs(it.generatedKotlinDirectory)
                        }

                        it.sourceFiles.set(project.objects.fileCollection().from(swiftSourceSets))
                        it.outputDir.set(extension.outputDir.dir(framework.name + File.separator + target.targetName))
                    }
                    framework.linkTask.finalizedBy(swiftCompileTaskProvider)
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

    private val KotlinSourceSet.generatedKotlinDirectory: String
        get() = "build/generated/swikt/$name/kotlin"
    private val KotlinSourceSet.generatedSwiftDirectory: String
        get() = "build/generated/swikt/$name/swift"
    private val KotlinSourceSet.kspSwiftDirectory: String
        get() = "build/generated/ksp/${name.dropLast(4)}/$name/resources"
    private val KotlinSourceSet.swiftSourceDirectory: String
        get() = "src/$name/swift"
    private val kspCommonSwiftDirectory = "build/generated/ksp/metadata/commonMain/resources"

    private val KotlinSourceSet.generateKotlinTaskName: String
        get() = "generate${name.capitalized()}Kotlin"
    private val KotlinSourceSet.generateSwiftTaskName: String
        get() = "generate${name.capitalized()}Swift"
    private val Framework.swiftCompileTaskName: String
        get() = listOf("swiftCompile", name.capitalized(), target.targetName.capitalized()).joinToString("")
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
