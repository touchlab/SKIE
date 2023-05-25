package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.features.SkieFeature
import org.gradle.api.provider.Property

internal infix fun SkieFeature.takeIf(property: Property<Boolean>): SkieFeature? =
    this.takeIf { property.orNull ?: false }
