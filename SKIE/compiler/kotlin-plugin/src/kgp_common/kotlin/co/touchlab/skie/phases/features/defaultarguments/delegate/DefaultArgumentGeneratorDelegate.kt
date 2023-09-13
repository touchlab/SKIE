package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.configuration.ConfigurationContainer

interface DefaultArgumentGeneratorDelegate : ConfigurationContainer {

    fun generate()
}
