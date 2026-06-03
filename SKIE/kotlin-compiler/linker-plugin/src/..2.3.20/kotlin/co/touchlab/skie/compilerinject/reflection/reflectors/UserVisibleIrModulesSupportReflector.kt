package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.utils.ResolvedDependency
import org.jetbrains.kotlin.backend.common.linkage.issues.UserVisibleIrModulesSupport

class UserVisibleIrModulesSupportReflector(
    override val instance: Any,
) : Reflector(UserVisibleIrModulesSupport::class) {

    val externalDependencyModules by declaredProperty<Collection<ResolvedDependency>>()
}
