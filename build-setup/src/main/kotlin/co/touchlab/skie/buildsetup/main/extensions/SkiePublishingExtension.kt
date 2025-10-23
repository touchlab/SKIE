package co.touchlab.skie.buildsetup.main.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class SkiePublishingExtension @Inject constructor(objects: ObjectFactory) {

    val name: Property<String> = objects.property()
    val description: Property<String> = objects.property()
}
