package co.touchlab.swikt.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

const val EXTENSION_NAME = "swikt"

abstract class SwiktPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create(EXTENSION_NAME, SwiktExtension::class.java, this)
        tasks.register("swiktTestScenarios", TestScenariosTask::class.java).configure {
            it.outputs.upToDateWhen { false }
        }

        afterEvaluate {
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
                }
            }
        }
    }

    private val KotlinSourceSet.generatedKotlinDirectory: String
        get() = "build/generated/swikt/$name/kotlin"
    private val KotlinSourceSet.generatedSwiftDirectory: String
        get() = "build/generated/swikt/$name/swift"
    private val KotlinSourceSet.swiftSourceDirectory: String
        get() = "src/$name/swift"

    private val KotlinSourceSet.generateKotlinTaskName: String
        get() = "generate${name.capitalized()}Kotlin"
    private val KotlinSourceSet.generateSwiftTaskName: String
        get() = "generate${name.capitalized()}Swift"
    private val Framework.swiftCompileTaskName: String
        get() = listOf("swiftCompile", name.capitalized(), target.targetName.capitalized()).joinToString("")
}
