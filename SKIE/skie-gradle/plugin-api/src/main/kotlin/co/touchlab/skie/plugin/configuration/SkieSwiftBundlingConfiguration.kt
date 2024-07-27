@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package co.touchlab.skie.plugin.configuration

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieSwiftBundlingConfiguration @Inject constructor(objects: ObjectFactory) {

    /**
     * Enables Swift bundling for this module.
     */
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}
