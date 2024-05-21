package co.touchlab.skie.plugin.shim

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer

interface KotlinNativeTargetShim : Named {

    val compilations: NamedDomainObjectContainer<KotlinNativeCompilationShim>
}
