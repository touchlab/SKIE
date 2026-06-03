@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compat.KonanConfig
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.UserVisibleIrModulesSupportReflector
import org.jetbrains.kotlin.utils.ResolvedDependency

internal fun getExternalDependencies(konanConfig: KonanConfig): Set<ResolvedDependency> =
    konanConfig.userVisibleIrModulesSupport
        .reflectedBy<UserVisibleIrModulesSupportReflector>()
        .externalDependencyModules
        .toSet()
