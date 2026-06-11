@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compat.KonanConfig
import org.jetbrains.kotlin.utils.ResolvedDependenciesSupport
import org.jetbrains.kotlin.utils.ResolvedDependency

/**
 * Kotlin 2.4.0 removed `UserVisibleIrModulesSupport` (KT-84684, "[PL] Clean-up: Remove UserVisibleIrModulesSupport
 * from IR linker"), which previously exposed the external (3rd-party) dependency modules together with their
 * coordinates and artifact paths.
 *
 * The underlying data is still available though: the build system (Kotlin Gradle plugin) still serializes the resolved
 * dependency graph into the file passed via `-Xexternal-dependencies` (stored on the compiler config as
 * [KonanConfig.externalDependenciesFile]), and the `ResolvedDependenciesSupport.deserialize` API is still public.
 *
 * The removed `UserVisibleIrModulesSupport.externalDependencyModules` (the only thing SKIE read) was simply
 * `deserialize(externalDependenciesFile).modules`, so we reproduce that here directly. The Kotlin/Native-specific
 * enrichment in the removed class (manifest versions, platform-library compression) only affected the linkage-error
 * display path (`getUserVisibleModules`), which SKIE never used.
 */
internal fun getExternalDependencies(konanConfig: KonanConfig): Set<ResolvedDependency> {
    val externalDependenciesFile = konanConfig.externalDependenciesFile ?: return emptySet()
    if (!externalDependenciesFile.exists) return emptySet()

    val externalDependenciesText = String(externalDependenciesFile.readBytes())

    return ResolvedDependenciesSupport
        .deserialize(externalDependenciesText) { _, _ -> }
        .modules
        .toSet()
}
