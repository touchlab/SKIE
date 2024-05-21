package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.skieInternalExtension
import co.touchlab.skie.plugin.util.named
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvedDependency

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

    passRuntimeDependencyToCompiler(skieRuntimeDependency)
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

    project.dependencies.add(skieRuntimeConfigurationName, BuildConfig.SKIE_KOTLIN_RUNTIME_COORDINATE)

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

private fun SkieTarget.passRuntimeDependencyToCompiler(skieRuntimeDependency: ResolvedDependency) {
    skieRuntimeDependency.moduleArtifacts
        .single { it.file.extension == "klib" }
        .let { moduleArtifact ->
            addFreeCompilerArgs(
                "-Xexport-library=${moduleArtifact.file.absolutePath}",
                "-library=${moduleArtifact.file.absolutePath}",
            )
        }
}

private val SkieTarget.skieRuntimeConfigurationName: String
    get() = "skieRuntimeFor" + linkerConfiguration.name.replaceFirstChar { it.uppercase() }

// Due to how KMP dependencies work there is a difference in the behavior of local and remote dependencies.
private fun ResolvedDependency.unwrapCommonKMPModule(): Set<ResolvedDependency> =
    if (moduleArtifacts.isEmpty()) children else setOf(this)
