package co.touchlab.skie.plugin.util

import org.gradle.api.provider.Property

internal infix fun <T> T.takeIf(property: Property<Boolean>): T? = this.takeIf { property.orNull ?: false }
