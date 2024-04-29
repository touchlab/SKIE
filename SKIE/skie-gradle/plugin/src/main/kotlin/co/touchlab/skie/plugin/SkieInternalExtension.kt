package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

internal interface SkieInternalExtension {

    val targets: NamedDomainObjectContainer<SkieTarget>

    // Used for local development because it requires a different path to the runtime dependency
    val runtimeVariantFallback: Property<Boolean>
}
