package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.utils.ResolvedDependency

expect class UserVisibleIrModulesSupportReflector(instance: Any) : Reflector {

    val externalDependencyModules: Collection<ResolvedDependency>
}
