package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.serialization.linkerissues.UserVisibleIrModulesSupport
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.utils.ResolvedDependency

class UserVisibleIrModulesSupportReflector(
    override val instance: UserVisibleIrModulesSupport,
) : Reflector(UserVisibleIrModulesSupport::class) {

    val externalDependencyModules by declaredProperty<Collection<ResolvedDependency>>()
}
