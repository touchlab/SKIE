package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel

class OriginalKotlinValueParameterSwiftModel(
    delegate: KotlinValueParameterSwiftModel,
) : KotlinValueParameterSwiftModel by delegate {

    override val argumentLabel: String = delegate.argumentLabel

    override val isChanged: Boolean = false
}
