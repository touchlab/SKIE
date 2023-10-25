package co.touchlab.skie.kir.configuration

import co.touchlab.skie.phases.features.flow.FlowMappingStrategy

class KirConfiguration(
    parent: KirConfiguration?,
) : KirConfigurationBase(parent) {

    var flowMappingStrategy by value(FlowMappingStrategy.None)
}
