@file:Suppress("MemberVisibilityCanBePrivate")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieMigrationConfiguration @Inject constructor(objects: ObjectFactory) {

    val wildcardExport: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * The Kotlin compiler by default exports `toString()` as `description()` and `hashCode()` as `hash()` in Swift.
     * This is technically an incorrect behavior because the NSObject defines these methods as properties.
     * Therefore, the Kotlin compiler should expose them as description and hash properties instead.
     *
     * This problem does not cause a compilation error only due to an unexpected interaction between the Kotlin compiler and the Swift compiler.
     * As a result, it's sometimes possible to call these methods both as a method and property in Swift.
     * So, for example: `description()` and `description` can be both used, but only if the Kotlin class overrides `toString()`.
     * Otherwise, only `description` can be used.
     *
     * SKIE corrects this behavior and always exports `toString()` as `description` and `hashCode()` as `hash`.
     * To enable the original behavior, set this flag to `true`.
     */
    val anyMethodsAsFunctions: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        setOfNotNull(
            SkieConfigurationFlag.Migration_WildcardExport takeIf wildcardExport,
            SkieConfigurationFlag.Migration_AnyMethodsAsFunctions takeIf anyMethodsAsFunctions,
        )
}
