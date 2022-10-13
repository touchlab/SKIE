package co.touchlab.swiftpack.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class SwiftPackExtension @Inject constructor(
    private val objectFactory: ObjectFactory,
) {

    val isPublishingEnabled: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
}