package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.configuration.ConfigurationContainer

internal interface DefaultArgumentGeneratorDelegate : ConfigurationContainer {

    fun generate()
}
