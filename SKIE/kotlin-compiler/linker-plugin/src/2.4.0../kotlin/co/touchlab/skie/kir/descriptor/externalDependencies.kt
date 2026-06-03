package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compat.KonanConfig
import org.jetbrains.kotlin.utils.ResolvedDependency

/**
 * Kotlin 2.4.0 removed `UserVisibleIrModulesSupport`, which previously exposed the external (3rd-party) dependency
 * modules together with their coordinates and artifact paths. There is no replacement that maps resolved libraries back
 * to their dependency coordinates, so external dependencies can no longer be reported.
 *
 * As a result, all non-built-in resolved libraries are treated as local modules (see
 * [NativeDescriptorProvider.localLibraries]) and the per-module analytics no longer distinguish external libraries.
 */
@Suppress("UNUSED_PARAMETER")
internal fun getExternalDependencies(konanConfig: KonanConfig): Set<ResolvedDependency> =
    emptySet()
