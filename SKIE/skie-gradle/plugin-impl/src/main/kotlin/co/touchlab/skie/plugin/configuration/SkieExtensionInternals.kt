@file:Suppress("MemberVisibilityCanBePrivate", "unused", "invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.SkieTarget
import org.gradle.api.Project

fun SkieExtension.Companion.createExtension(project: Project): SkieExtension = project.extensions.create("skie", SkieExtension::class.java)

val Project.skieExtension: SkieExtension
    get() = project.extensions.getByType(SkieExtension::class.java)

fun SkieExtension.buildConfiguration(target: SkieTarget): GradleSkieConfigurationData = GradleSkieConfigurationData(
    enabledConfigurationFlags = getUserConfiguredFlags() + target.requiredConfigurationFlags,
    groups = features.buildGroups() + build.buildGroup(),
)

private fun SkieExtension.getUserConfiguredFlags(): Set<SkieConfigurationFlag> =
    (mergeConfigurationSetsFromConfigurations() + additionalConfigurationFlags.get()) - suppressedConfigurationFlags.get()

private fun SkieExtension.mergeConfigurationSetsFromConfigurations(): Set<SkieConfigurationFlag> = analytics.buildConfigurationFlags() +
    build.buildConfigurationFlags() +
    debug.buildConfigurationFlags() +
    features.buildConfigurationFlags() +
    migration.buildConfigurationFlags()

private fun SkieFeatureConfiguration.buildGroups(): List<GradleSkieConfigurationData.Group> = groupConfigurations.map { it.build() }

private fun SkieFeatureConfiguration.GroupConfiguration.build(): GradleSkieConfigurationData.Group = GradleSkieConfigurationData.Group(
    target = targetFqNamePrefix,
    overridesAnnotations = overridesAnnotations,
    items = items.toMap(),
)

private fun SkieBuildConfiguration.buildGroup(): GradleSkieConfigurationData.Group = GradleSkieConfigurationData.Group(
    target = "",
    overridesAnnotations = false,
    items = buildItems(),
)
