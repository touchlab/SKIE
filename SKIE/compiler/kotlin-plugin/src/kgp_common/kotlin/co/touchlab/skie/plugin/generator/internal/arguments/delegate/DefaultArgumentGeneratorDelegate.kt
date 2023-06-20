package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.plugin.api.kotlin.DescriptorRegistrationScope
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer

internal interface DefaultArgumentGeneratorDelegate : ConfigurationContainer {

    fun generate()
}
