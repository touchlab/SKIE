package co.touchlab.skie.gradle.kotlin

import org.gradle.api.provider.Property

abstract class JvmExtension {

    abstract val areContextReceiversEnabled: Property<Boolean>

    init {
        @Suppress("LeakingThis")
        areContextReceiversEnabled.convention(false)
    }
}
