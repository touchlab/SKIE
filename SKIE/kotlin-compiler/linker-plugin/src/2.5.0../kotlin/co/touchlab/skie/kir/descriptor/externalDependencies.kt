@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compat.KonanConfig
import org.jetbrains.kotlin.utils.ResolvedDependenciesSupport
import org.jetbrains.kotlin.utils.ResolvedDependency
import kotlin.io.path.exists
import kotlin.io.path.readBytes

internal fun getExternalDependencies(konanConfig: KonanConfig): Set<ResolvedDependency> {
    val externalDependenciesFile = konanConfig.externalDependenciesFile ?: return emptySet()
    if (!externalDependenciesFile.exists()) return emptySet()

    val externalDependenciesText = String(externalDependenciesFile.readBytes())

    return ResolvedDependenciesSupport
        .deserialize(externalDependenciesText) { _, _ -> }
        .modules
        .toSet()
}
