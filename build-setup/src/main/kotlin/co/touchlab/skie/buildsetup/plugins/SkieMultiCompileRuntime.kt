package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.plugins.util.MultiCompileRuntimeExtension
import co.touchlab.skie.buildsetup.plugins.util.MultiCompileTarget
import co.touchlab.skie.buildsetup.plugins.util.MultiCompileTarget.Companion.kotlin_2_1_0
import co.touchlab.skie.buildsetup.tasks.BuildNestedGradle
import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages

typealias PublishTaskNamesWithTasks = Map<Pair<String, List<String>>, TaskProvider<Task>>
typealias SupportedTargetsWithDeclarations = List<Pair<MultiCompileTarget, String>>

class SkieMultiCompileRuntime : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        apply<BaseKotlin>()

        val extension = extensions.create<MultiCompileRuntimeExtension>("multiCompileRuntime")
        afterEvaluate {
            val publishTaskNamesWithTasks: PublishTaskNamesWithTasks? = if (extension.isPublishable.get()) {
                publishTaskNames.associateWith { (publishTaskName, _) ->
                    tasks.register(publishTaskName)
                }
            } else {
                null
            }

            setupRootTasksIfNeeded(extension, publishTaskNamesWithTasks)

            kotlinToolingVersionDimension().components.forEach { kotlinToolingVersion ->
                val pathSafeKotlinVersionName = kotlinToolingVersion.name.toString().replace('.', '_')
                val supportedTargetsWithDeclarations = extension.supportedTargetsWithDeclarations(kotlinToolingVersion.name)
                val copyTask = registerCopyTask(
                    extension = extension,
                    name = kotlinToolingVersion.name.toString(),
                    kotlinVersion = kotlinToolingVersion.primaryVersion,
                    artifactIdSuffix = "-${kotlinToolingVersion.name}",
                    smokeTestTmpRepositoryPath = smokeTestTmpRepositoryPathOrNull,
                )

                val buildTask = registerBuildTask(
                    name = kotlinToolingVersion.name.toString(),
                    supportedTargetsWithDeclarations = supportedTargetsWithDeclarations,
                    kotlinVersion = kotlinToolingVersion.name,
                    copyTask = copyTask,
                )

                if (publishTaskNamesWithTasks != null) {
                    registerPublishTask(
                        name = kotlinToolingVersion.name.toString(),
                        copyTask = copyTask,
                        publishTaskNamesWithTasks = publishTaskNamesWithTasks,
                    )
                }

                supportedTargetsWithDeclarations.forEach { (target, _) ->
                    val configuration = configurations.create("${target.name}__kgp_${kotlinToolingVersion.name}") {
                        isCanBeConsumed = true
                        isCanBeResolved = false

                        attributes {
                            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
                            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
                            attribute(KotlinPlatformType.attribute, target.platformType)
                            attribute(
                                TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                                objects.named(
                                    if (target.platformType == KotlinPlatformType.jvm) {
                                        TargetJvmEnvironment.STANDARD_JVM
                                    } else {
                                        "non-jvm"
                                    },
                                ),
                            )
                            attribute(KotlinNativeTarget.konanTargetAttribute, target.konanTargetName)
                            attribute(KotlinCompilerVersionAttribute.attribute, objects.named(kotlinToolingVersion.name.toString()))
                        }
                    }

                    dependencies {
                        extension.applyDependencies.get().invoke(this, kotlinToolingVersion.name, configuration)
                    }

                    val klibPath = extension.klibPath.get().invoke(kotlinToolingVersion.name, target)
                    artifacts.add(configuration.name, copyTask.map { it.destinationDir.resolve(klibPath) }) {
                        builtBy(buildTask)
                        classifier = "${target.name}-kgp_${pathSafeKotlinVersionName}"
                    }
                }
            }

        }
    }

    private fun MultiCompileRuntimeExtension.supportedTargetsWithDeclarations(
        kotlinVersion: KotlinToolingVersion,
    ): List<Pair<MultiCompileTarget, String>> {
        return targets.get().mapNotNull { target ->
            target.declaration(kotlinVersion)?.let {
                target to it
            }
        }
    }

    private val Project.smokeTestTmpRepositoryPathOrNull: String?
        get() {
            val smokeTestTmpRepositoryPath: String? by project
            return smokeTestTmpRepositoryPath
        }

    private val Project.publishTaskNames: List<Pair<String, List<String>>>
        get() = listOfNotNull(
            "publishToMavenLocal" to listOf("publishToMavenLocal"),
            "publishToSonatype" to listOf("findSonatypeStagingRepository", "publishToSonatype"),
            if (smokeTestTmpRepositoryPathOrNull != null) {
                "publishAllPublicationsToSmokeTestTmpRepository" to listOf("publishAllPublicationsToSmokeTestTmpRepository")
            } else {
                null
            },
        )

    private fun Project.setupRootTasksIfNeeded(
        extension: MultiCompileRuntimeExtension,
        publishTaskNamesWithTasks: PublishTaskNamesWithTasks?,
    ) {
        val rootKotlinVersion = extension.rootKotlinVersion.orNull ?: return
        val copyTask = registerCopyTask(extension, "root", rootKotlinVersion, "", smokeTestTmpRepositoryPathOrNull)
        if (publishTaskNamesWithTasks != null) {
            registerPublishTask("root", copyTask, publishTaskNamesWithTasks)
        }
    }

    private fun Project.registerCopyTask(
        extension: MultiCompileRuntimeExtension,
        name: String,
        kotlinVersion: KotlinToolingVersion,
        artifactIdSuffix: String,
        smokeTestTmpRepositoryPath: String?,
    ): TaskProvider<Copy> {
        return tasks.register<Copy>("copyProject__$name") {
            group = "other"
            description = "Copy project files for Kotlin $name"

            val tokens = provider {
                mapOf(
                    "targetKotlinVersion" to kotlinVersion.toString(),
                    "artifactIdSuffix" to artifactIdSuffix,
                    "targets" to extension.supportedTargetsWithDeclarations(kotlinVersion).joinToString("\n") { (_, declaration) -> declaration },
                    "dependencies" to extension.dependencies.get().invoke(kotlinVersion),
                    "smokeTestTmpRepositoryConfiguration" to smokeTestTmpRepositoryPath?.let {
                        """
                            publishing {
                                repositories {
                                    maven {
                                        url = uri("$it")
                                        name = "smokeTestTmp"
                                    }
                                }
                            }
                        """.trimIndent()
                    }.orEmpty(),
                )
            }

            inputs.property("tokens", tokens)
            from(extension.sourceDir) {
                include(extension.sourceIncludes.get())
                filter(
                    ReplaceTokens::class,
                    "tokens" to tokens.get(),
                )
            }
            into(layout.buildDirectory.dir("${this@registerCopyTask.name}_$name"))
        }
    }

    private fun Project.registerBuildTask(
        name: String,
        kotlinVersion: KotlinToolingVersion,
        supportedTargetsWithDeclarations: SupportedTargetsWithDeclarations,
        copyTask: TaskProvider<Copy>,
    ): TaskProvider<BuildNestedGradle> {
        return tasks.register<BuildNestedGradle>("buildProject__$name") {
            group = "build"

            dependsOn(copyTask)

            projectDir.fileProvider(copyTask.map { it.destinationDir })

            tasks.set(
                supportedTargetsWithDeclarations.flatMap { (target, _) ->
                    when (target.platformType) {
                        KotlinPlatformType.common -> listOf("metadataMainClasses")
                        KotlinPlatformType.jvm,
                        KotlinPlatformType.androidJvm,
                        KotlinPlatformType.js,
                        KotlinPlatformType.wasm,
                            -> listOf("${target.name}Jar")
                        KotlinPlatformType.native -> if (kotlinVersion >= kotlin_2_1_0) {
                            listOf("${target.name}Klib")
                        } else {
                            listOf("${target.name}MainKlibrary")
                        }
                    }
                },
            )
        }
    }

    private fun Project.registerPublishTask(
        name: String,
        copyTask: TaskProvider<Copy>,
        publishTaskNamesWithTasks: Map<Pair<String, List<String>>, TaskProvider<Task>>,
    ) {
        publishTaskNamesWithTasks.forEach { (publishTaskNames, parentPublishTask) ->
            val (publishTaskName, publishTasks) = publishTaskNames
            val publishTask = tasks.register<BuildNestedGradle>("${publishTaskName}__$name") {
                group = "publishing"

                dependsOn(copyTask)

                projectDir.fileProvider(copyTask.map { it.destinationDir })

                tasks.set(publishTasks)
            }

            parentPublishTask.configure {
                dependsOn(publishTask)
            }
        }
    }
}
