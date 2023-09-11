package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.utils.ResolvedDependency

expect class UserVisibleIrModulesSupportReflector(instance: Any) : Reflector {

    val externalDependencyModules: Collection<ResolvedDependency>
}
