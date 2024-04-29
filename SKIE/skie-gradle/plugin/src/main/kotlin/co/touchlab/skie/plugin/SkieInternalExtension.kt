package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

internal interface SkieInternalExtension {

    val targets: NamedDomainObjectContainer<SkieTarget>
}
