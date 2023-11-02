package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

internal interface SkieInternalExtension {
    val targets: NamedDomainObjectContainer<SkieTarget>

    val runtimeVariantFallback: Property<Boolean>
}
