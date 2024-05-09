@file:Suppress("MemberVisibilityCanBePrivate", "unused", "invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.SkieTarget
import org.gradle.api.Project

internal fun SkieExtension.Companion.createExtension(project: Project): SkieExtension =
    project.extensions.create("skie", SkieExtension::class.java)

internal val Project.skieExtension: SkieExtension
    get() = project.extensions.getByType(SkieExtension::class.java)

internal fun SkieExtension.buildConfiguration(outputKind: SkieTarget.OutputKind): GradleSkieConfigurationData =
    GradleSkieConfigurationData(
        enabledConfigurationFlags = (mergeConfigurationSetsFromConfigurations(outputKind) + additionalConfigurationFlags.get()) - suppressedConfigurationFlags.get(),
        groups = features.buildGroups(),
    )

private fun SkieExtension.mergeConfigurationSetsFromConfigurations(outputKind: SkieTarget.OutputKind): Set<SkieConfigurationFlag> =
    analytics.buildConfigurationFlags() +
        build.buildConfigurationFlags() +
        debug.buildConfigurationFlags() +
        features.buildConfigurationFlags() +
        migration.buildConfigurationFlags() +
        addSwiftLibraryEvolutionFlagIfNeeded(outputKind)

private fun addSwiftLibraryEvolutionFlagIfNeeded(outputKind: SkieTarget.OutputKind): Set<SkieConfigurationFlag> =
    if (outputKind == SkieTarget.OutputKind.XCFramework) {
        setOf(SkieConfigurationFlag.Build_SwiftLibraryEvolution)
    } else {
        emptySet()
    }

private fun SkieFeatureConfiguration.buildGroups(): List<GradleSkieConfigurationData.Group> =
    groupConfigurations.map { it.build() }

private fun SkieFeatureConfiguration.GroupConfiguration.build(): GradleSkieConfigurationData.Group =
    GradleSkieConfigurationData.Group(
        target = targetFqNamePrefix,
        overridesAnnotations = overridesAnnotations,
        items = items.toMap(),
    )
