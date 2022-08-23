package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import javax.inject.Inject

open class SwiftGenExtension @Inject constructor(
    objects: ObjectFactory,
) {

    private val propertyProvider = PropertyProvider(objects, ::SwiftGenConfiguration)

    private val sealedInteropDefaults = objects.newInstance(SealedInteropDefaultsHandler::class.java, propertyProvider)

    fun toSubpluginOptions(): List<SubpluginOption> =
        propertyProvider.toSubpluginOptions()

    fun sealedInteropDefaults(action: Action<SealedInteropDefaultsHandler>) {
        action.execute(sealedInteropDefaults)
    }

    open class SealedInteropDefaultsHandler @Inject constructor(
        propertyProvider: PropertyProvider<SwiftGenConfiguration>,
    ) {

        var enabled by propertyProvider.property { sealedInteropDefaults::enabled }
        var functionName by propertyProvider.property { sealedInteropDefaults::functionName }
        var elseName by propertyProvider.property { sealedInteropDefaults::elseName }
        var visibleCases by propertyProvider.property { sealedInteropDefaults::visibleCases }
    }
}