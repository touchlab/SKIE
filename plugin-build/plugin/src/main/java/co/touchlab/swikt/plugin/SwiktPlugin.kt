package co.touchlab.swikt.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

const val EXTENSION_NAME = "swikt"

abstract class SwiktPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create(EXTENSION_NAME, SwiktExtension::class.java, this)

        afterEvaluate {
            val kotlin = extensions.findByType<KotlinMultiplatformExtension>() ?: return@afterEvaluate
            val appleTargets = kotlin.targets
                .mapNotNull { it as? KotlinNativeTarget }
                .filter { it.konanTarget.family.isAppleFamily }

            appleTargets.forEach { target ->
                val frameworks = target.binaries.mapNotNull { it as? Framework }
                frameworks.forEach { framework ->
                    if (!framework.isStatic) {
                        framework.linkTask.doFirst {
                            framework.isStatic = true
                        }
                        framework.linkTask.doLast {
                            framework.isStatic = false
                        }
                    }

                    val swiftCompileTaskProvider = tasks.register(framework.swiftCompileTaskName, SwiftCompileTask::class.java, framework)
                    swiftCompileTaskProvider.configure {
                        val target = framework.target
                        it.description = "Compiles Swift code for ${framework.outputKind.description} '${framework.name}' for a target '${target.name}'."
                        it.enabled = framework.linkTask.enabled
                        it.outputs.upToDateWhen { framework.linkTask.state.upToDate }

                        val swiftSourceSets = framework.compilation.allKotlinSourceSets.map { kotlinSourceSet ->
                            "${kotlinSourceSet.name} Swift source".let {
                                project.objects.sourceDirectorySet(it, it).apply {
                                    filter.include("**/*.swift")
                                    srcDirs("src/${kotlinSourceSet.name}/swift")
                                }
                            }
                        }

                        it.sourceFiles.set(project.objects.fileCollection().from(swiftSourceSets))
                        it.outputDir.set(extension.outputDir.dir(framework.name + File.separator + target.targetName))
                    }
                    framework.linkTask.finalizedBy(swiftCompileTaskProvider)
                }
            }
        }
    }

    private val Framework.swiftCompileTaskName: String
        get() = listOf("swiftCompile", name.capitalized(), target.targetName.capitalized()).joinToString("")

}
