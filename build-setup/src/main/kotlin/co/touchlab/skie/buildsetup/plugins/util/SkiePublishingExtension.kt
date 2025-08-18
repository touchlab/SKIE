package co.touchlab.skie.buildsetup.plugins.util

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class SkiePublishingExtension @Inject constructor(objects: ObjectFactory) {

    val publishSources: Property<Boolean> = objects.property<Boolean>().convention(true)
    val publishJavadoc: Property<Boolean> = objects.property<Boolean>().convention(false)

    val name: Property<String> = objects.property()
    val description: Property<String> = objects.property()
}
