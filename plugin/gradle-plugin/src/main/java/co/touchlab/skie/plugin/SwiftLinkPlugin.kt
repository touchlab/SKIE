package co.touchlab.skie.plugin

import co.touchlab.skie.gradle_plugin.BuildConfig
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkLayout
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Architecture
import java.io.File

const val EXTENSION_NAME = "skie"

const val SWIFT_LINK_PLUGIN_CONFIGURATION_NAME = "skiePlugin"

// We need to use an anonymous class instead of lambda to keep execution optimizations.
// https://docs.gradle.org/7.4.2/userguide/validation_problems.html#implementation_unknown
@Suppress("ObjectLiteralToLambda")
abstract class SwiftLinkPlugin : Plugin<Project> {

    private fun createSwiftGenConfiguration(project: Project): TaskProvider<CreateSwiftGenConfigTask> {
        val task = project.tasks.register<CreateSwiftGenConfigTask>(CreateSwiftGenConfigTask.name)

        project.tasks.withType<KotlinNativeLink> {
            inputs.file(task.map { it.configFile })
        }

        return task
    }

    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create(EXTENSION_NAME, SkieExtension::class.java)
        val createSwiftGenConfigTask = createSwiftGenConfiguration(project)

        val swiftLinkPluginConfiguration = configurations.maybeCreate(SWIFT_LINK_PLUGIN_CONFIGURATION_NAME).apply {
            isCanBeResolved = true
            isCanBeConsumed = false
            isVisible = false
            isTransitive = true

            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                it.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            }
        }

        // WORKAROUND: Fix fat framework name for CocoaPods plugin.
        pluginManager.withPlugin("kotlin-native-cocoapods") {
            tasks.withType<FatFrameworkTask>().matching { it.name == "fatFramework" }.configureEach { task ->
                // Unfortunately has to be done in `doFirst` to make sure the task is already configured by the plugin when we run our code
                task.doFirst(object : Action<Task> {
                    override fun execute(p0: Task) {
                        val commonFrameworkName = task.frameworks.map { it.name }.distinct().singleOrNull() ?: return
                        task.baseName = commonFrameworkName
                    }
                })
            }
        }

        afterEvaluate {
            val swiftKtCompilerPluginConfiguration = configurations.create("swiftKtCompilerPlugin") {
                it.isCanBeConsumed = false
                it.isCanBeResolved = true

                it.exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
            }

            dependencies {
                swiftKtCompilerPluginConfiguration(
                    group = BuildConfig.KOTLIN_PLUGIN_GROUP,
                    name = BuildConfig.KOTLIN_PLUGIN_NAME,
                    version = BuildConfig.KOTLIN_PLUGIN_VERSION
                )
            }

            val swiftLinkSubplugins = plugins.withType<SwiftLinkSubplugin>()
            swiftLinkSubplugins.forEach { it.configureDependencies(project, swiftLinkPluginConfiguration) }

            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: return@afterEvaluate
            val appleTargets = kotlin.targets
                .mapNotNull { it as? KotlinNativeTarget }
                .filter { it.konanTarget.family.isAppleFamily }

            appleTargets.forEach { target ->
                target.registerRuntime(extension)

                val frameworks = target.binaries.mapNotNull { it as? Framework }
                frameworks.forEach { framework ->
                    val subpluginOptions = swiftLinkSubplugins.associateWith { subplugin ->
                        subplugin.getOptions(project, framework)
                    }

                    framework.linkTaskProvider.configure { linkTask ->
                        val defaultSwiftSourceSet = configureSwiftSourceSet(framework.compilation.defaultSourceSet)
                        val allSwiftSourceSets = (framework.compilation.allKotlinSourceSets - framework.compilation.defaultSourceSet)
                            .map { configureSwiftSourceSet(it) } + listOf(defaultSwiftSourceSet)

                        linkTask.dependsOn(createSwiftGenConfigTask)

                        // TODO: linkTask.inputs

                        val swiftSources = project.objects.fileCollection().from(allSwiftSourceSets)

                        linkTask.compilerPluginClasspath = listOfNotNull(
                            linkTask.compilerPluginClasspath,
                            swiftKtCompilerPluginConfiguration,
                            swiftLinkPluginConfiguration
                        ).reduce(FileCollection::plus)

                        linkTask.compilerPluginOptions.addPluginArgument(
                            SkiePlugin.id, SkiePlugin.Options.generatedSwiftDir.subpluginOption(
                                layout.buildDirectory.dir("generated/swift/${framework.name}/${framework.target.targetName}").get().asFile
                            )
                        )
                        linkTask.compilerPluginOptions.addPluginArgument(
                            SkiePlugin.id, SkiePlugin.Options.disableWildcardExport.subpluginOption(
                                extension.isWildcardExportPrevented.get()
                            )
                        )

                        linkTask.compilerPluginOptions.addPluginArgument(
                            SkiePlugin.id, SkiePlugin.Options.swiftGenConfigPath.subpluginOption(createSwiftGenConfigTask.get().configFile)
                        )

                        swiftSources.forEach { swiftFile ->
                            linkTask.compilerPluginOptions.addPluginArgument(
                                SkiePlugin.id,
                                SkiePlugin.Options.swiftSourceFile.subpluginOption(swiftFile)
                            )
                        }

                        subpluginOptions.forEach { (subplugin, options) ->
                            options.get().forEach {
                                linkTask.compilerPluginOptions.addPluginArgument(subplugin.compilerPluginId, it)
                            }
                        }
                    }
                }
            }

            tasks.withType<FatFrameworkTask>().configureEach { task ->
                task.doLast(object : Action<Task> {
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

                        frameworksByArchs.toList().forEach { (_, framework) ->
                            it.copy {
                                it.from(framework.files.apiNotes)
                                it.into(target.headerDir)
                            }
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

    private fun KotlinNativeTarget.registerRuntime(skieExtension: SkieExtension) {
        if (!skieExtension.features.suspendInterop.get()) {
            return
        }

        this.compilations.named("main") {
            it.defaultSourceSet.dependencies {
                api(BuildConfig.RUNTIME_DEPENDENCY)
            }
        }

        this.binaries.withType<AbstractNativeLibrary>().configureEach {
            it.export(BuildConfig.RUNTIME_DEPENDENCY)
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

    private inline fun <T : Task> TaskProvider<T>.configuring(crossinline configuration: T.() -> Unit): TaskProvider<T> {
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

val FrameworkLayout.apiNotes: File
    get() = headerDir.resolve("$frameworkName.apinotes")

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

fun <T> PluginOption<T>.subpluginOption(value: T): SubpluginOption = SubpluginOption(optionName, serialize(value))
