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

    /**
     * Global configuration for the sealed interop.
     */
    fun sealedInteropDefaults(action: Action<SealedInteropDefaultsHandler>) {
        action.execute(sealedInteropDefaults)
    }

    open class SealedInteropDefaultsHandler @Inject constructor(
        propertyProvider: PropertyProvider<SwiftGenConfiguration>,
    ) {

        /**
         * If true, the interop code is generated for all sealed classes/interfaces
         * for which the plugin is not explicitly disabled via an annotation.
         * Otherwise, the code is generated only for explicitly annotated sealed classes/interfaces.
         */
        var enabled by propertyProvider.property { sealedInteropDefaults::enabled }

        /**
         * The default name for the function used inside `switch`.
         */
        var functionName by propertyProvider.property { sealedInteropDefaults::functionName }

        /**
         * The default name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
         */
        var elseName by propertyProvider.property { sealedInteropDefaults::elseName }

        /**
         * If true the enum cases are generated for all direct children of sealed class/interface that are visible from Swift.
         * This behavior can be overridden on a case by case basis by an annotation.
         * If false, each child must be explicitly annotated, otherwise it will be considered as hidden.
         */
        var visibleCases by propertyProvider.property { sealedInteropDefaults::visibleCases }
    }
}