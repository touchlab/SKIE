package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class SkiePublishingExtension @Inject constructor(objects: ObjectFactory) {

    val publishSources: Property<Boolean> = objects.property<Boolean>().convention(true)

    val name: Property<String> = objects.property()
    val description: Property<String> = objects.property()
}
