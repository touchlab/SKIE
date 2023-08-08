package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.linkage.issues.UserVisibleIrModulesSupport
import org.jetbrains.kotlin.utils.ResolvedDependency

actual class UserVisibleIrModulesSupportReflector actual constructor(
    override val instance: Any,
) : Reflector(UserVisibleIrModulesSupport::class) {

    actual val externalDependencyModules by declaredProperty<Collection<ResolvedDependency>>()
}
