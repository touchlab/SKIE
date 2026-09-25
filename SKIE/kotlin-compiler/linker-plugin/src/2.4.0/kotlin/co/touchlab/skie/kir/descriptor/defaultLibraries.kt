@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.backend.konan.NativeSecondStageCompilationConfig
import org.jetbrains.kotlin.konan.library.isFromKotlinNativeDistribution
import org.jetbrains.kotlin.library.KotlinLibrary

internal fun getDefaultLibraries(konanConfig: NativeSecondStageCompilationConfig): Set<KotlinLibrary> =
    konanConfig.resolvedLibraries.getFullList()
        .filter { it.isFromKotlinNativeDistribution }
        .toSet()
