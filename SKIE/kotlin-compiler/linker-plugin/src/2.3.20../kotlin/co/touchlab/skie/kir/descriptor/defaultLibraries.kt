@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.library.KotlinLibrary

internal fun getDefaultLibraries(konanConfig: KonanConfig): Set<KotlinLibrary> =
    konanConfig.resolvedLibraries.getFullResolvedList().filter { it.isDefault }.map { it.library }.toSet()
