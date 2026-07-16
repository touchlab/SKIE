@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.backend.konan.NativeSecondStageCompilationConfig
import org.jetbrains.kotlin.konan.library.isImplicitlyLoadedFromKotlinNativeDistribution
import org.jetbrains.kotlin.library.KotlinLibrary

/**
 * Kotlin 2.4.20 split `KotlinLibrary.isFromKotlinNativeDistribution` into
 * `isImplicitlyLoadedFromKotlinNativeDistribution` and `isExplicitlySpecifiedByUserInCLIArgument`.
 */
internal fun getDefaultLibraries(konanConfig: NativeSecondStageCompilationConfig): Set<KotlinLibrary> =
    konanConfig.resolvedLibraries.getFullList()
        .filter { it.isImplicitlyLoadedFromKotlinNativeDistribution }
        .toSet()
