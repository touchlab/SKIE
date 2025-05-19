package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.skieInternalExtension
import co.touchlab.skie.plugin.util.named
import co.touchlab.skie.util.file.isKlib
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.component.ModuleComponentSelector

fun Project.configureSkieConfigurationAnnotationsDependencySubstitution() {
    val configurationAnnotationsRegex = "${Regex.escape(BuildConfig.SKIE_CONFIGURATION_ANNOTATIONS_MODULE)}(?:-[0-9]+\\.[0-9]+\\.[0-9]+)?(-.+)?".toRegex()
    skieInternalExtension.targets.configureEach {
        linkerConfiguration.apply {
            if (state != Configuration.State.UNRESOLVED) {
                return@configureEach
            }

            resolutionStrategy {
                dependencySubstitution {
                    all {
                        val requestedModule = requested as? ModuleComponentSelector ?: return@all
                        val match = configurationAnnotationsRegex.matchEntire(requestedModule.moduleIdentifier.toString())
                        if (match != null) {
                            val suffix = match.groupValues[1]

                            val updatedCoordinate = BuildConfig.SKIE_CONFIGURATION_ANNOTATIONS_MODULE + "-" + skieInternalExtension.kotlinVersion + suffix + ":" + BuildConfig.SKIE_VERSION

                            logger.debug("Replacing {} with {}", requested, updatedCoordinate)
                            useTarget(updatedCoordinate)
                        }
                    }
                }
            }
        }
    }
}

// Workaround for dependency pollution caused by SKIE prior 0.7.0 where SKIE added the runtime to published configurations.
// As a result, some libraries have dependencies on the runtime potentially with the wrong version of Kotlin.
fun Project.configureSkieRuntimeDependencySubstitution() {
    configurations.configureEach {
        if (state != Configuration.State.UNRESOLVED) {
            return@configureEach
        }

        resolutionStrategy {
            dependencySubstitution {
                all {
                    val requestedModule = requested as? ModuleComponentSelector ?: return@all

                    if (requestedModule.moduleIdentifier.toString().startsWith(BuildConfig.SKIE_KOTLIN_RUNTIME_MODULE) && requestedModule.module.contains("__kgp_")) {
                        val baseTargetModuleId = requestedModule.toString().removePrefix(BuildConfig.SKIE_KOTLIN_RUNTIME_MODULE).substringBefore("__kgp_")

                        val updatedCoordinate = "${BuildConfig.SKIE_KOTLIN_RUNTIME_MODULE}-${skieInternalExtension.kotlinVersion}${baseTargetModuleId}:${BuildConfig.SKIE_VERSION}"

                        logger.debug("Replacing {} with {}", requested, updatedCoordinate)
                        useTarget(updatedCoordinate)
                    }
                }
            }
        }
    }
}

fun SkieTarget.addDependencyOnSkieRuntime() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    val skieRuntimeConfiguration = getOrCreateSkieRuntimeConfiguration()

    linkerConfiguration.incoming.afterResolve {
        registerSkieRuntime(skieRuntimeConfiguration)
    }
}

private fun SkieTarget.registerSkieRuntime(
    skieRuntimeConfiguration: Configuration,
) {
    val skieRuntimeDependency = skieRuntimeConfiguration.getSkieRuntimeDependency()

    val skieRuntimeDirectDependencies = skieRuntimeDependency.getSkieRuntimeDirectDependencies()

    val linkerDependenciesIds = linkerConfiguration.resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id.module }

    if (!areCoroutinesUsedInProject(skieRuntimeDirectDependencies, linkerDependenciesIds)) {
        return
    }

    verifyAllRuntimeDependenciesAreAvailable(skieRuntimeDirectDependencies, linkerDependenciesIds)

    passRuntimeDependencyToCompiler(skieRuntimeDependency, linkerConfiguration.resolvedConfiguration)
}

private fun SkieTarget.getOrCreateSkieRuntimeConfiguration(): Configuration {
    val skieRuntimeConfigurationName = skieRuntimeConfigurationName

    project.configurations.findByName(skieRuntimeConfigurationName)?.let {
        return it
    }

    val skieRuntimeConfiguration = project.configurations.create(skieRuntimeConfigurationName) {
        attributes {
            project.kgpShim.addKmpAttributes(this, konanTarget)
            attribute(KotlinCompilerVersion.attribute, project.objects.named(project.skieInternalExtension.kotlinVersion))
        }
    }

    val runtimeCoordinate = listOf(
        BuildConfig.SKIE_KOTLIN_RUNTIME_GROUP,
        "${BuildConfig.SKIE_KOTLIN_RUNTIME_NAME}-${project.skieInternalExtension.kotlinVersion}",
        BuildConfig.SKIE_KOTLIN_RUNTIME_VERSION,
    ).joinToString(":")
    project.dependencies.add(skieRuntimeConfigurationName, runtimeCoordinate)

    return skieRuntimeConfiguration
}

private fun Configuration.getSkieRuntimeDependency(): ResolvedDependency =
    resolvedConfiguration.firstLevelModuleDependencies
        .single()
        .unwrapCommonKMPModule()
        .single()

private fun ResolvedDependency.getSkieRuntimeDirectDependencies(): List<ModuleIdentifier> =
    children.flatMap { it.unwrapCommonKMPModule() }.map { it.module.id.module }

private fun areCoroutinesUsedInProject(
    skieRuntimeDirectDependencies: List<ModuleIdentifier>,
    linkerDependenciesIds: List<ModuleIdentifier>,
): Boolean {
    val coroutinesDependency = skieRuntimeDirectDependencies.single { it.name.startsWith("kotlinx-coroutines") }

    return coroutinesDependency in linkerDependenciesIds
}

private fun SkieTarget.verifyAllRuntimeDependenciesAreAvailable(
    skieRuntimeDirectDependencies: List<ModuleIdentifier>,
    linkerDependenciesIds: List<ModuleIdentifier>,
) {
    skieRuntimeDirectDependencies.forEach {
        // KGP 1.9.10 and older handle stdlib differently and because of this difference the artifact is not present in the linker configuration
        if (it !in linkerDependenciesIds && it.toString() != "org.jetbrains.kotlin:kotlin-stdlib") {
            throw IllegalStateException(
                "SKIE runtime requires a dependency '$it' which the target's configuration '${linkerConfiguration.name}' does not have. " +
                    "This is most likely a bug in SKIE.",
            )
        }
    }
}

private fun SkieTarget.passRuntimeDependencyToCompiler(skieRuntimeDependency: ResolvedDependency, linkerResolvedConfiguration: ResolvedConfiguration) {
    val runtimeArtifact = skieRuntimeDependency.moduleArtifacts.single { it.file.isKlib }

    // Ensures that SKIE does not add the runtime for the second time if it is already present due to the issue with dependency pollution.
    val existingDependency = linkerResolvedConfiguration.resolvedArtifacts.firstOrNull {
        it.moduleVersion.id.module == runtimeArtifact.moduleVersion.id.module
    }

    if (existingDependency == null) {
        addFreeCompilerArgs(
            "-Xexport-library=${runtimeArtifact.file.absolutePath}",
            "-library=${runtimeArtifact.file.absolutePath}",
        )
    } else {
        addFreeCompilerArgs(
            "-Xexport-library=${existingDependency.file.absolutePath}",
        )
    }
}

private val SkieTarget.skieRuntimeConfigurationName: String
    get() = "skieRuntimeFor" + linkerConfiguration.name.replaceFirstChar { it.uppercase() }

// Due to how KMP dependencies work there is a difference in the behavior of local and remote dependencies.
private fun ResolvedDependency.unwrapCommonKMPModule(): Set<ResolvedDependency> =
    if (moduleArtifacts.isEmpty()) children else setOf(this)
