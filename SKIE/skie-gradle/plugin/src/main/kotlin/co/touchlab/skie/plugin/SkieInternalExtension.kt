package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.shim.KgpShim
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

internal abstract class SkieInternalExtension {

    abstract val targets: NamedDomainObjectContainer<SkieTarget>

    lateinit var kgpShim: KgpShim
}

internal val Project.skieInternalExtension: SkieInternalExtension
    get() = extensions.getByType(SkieInternalExtension::class.java)

internal val Project.kgpShim: KgpShim
    get() = skieInternalExtension.kgpShim
