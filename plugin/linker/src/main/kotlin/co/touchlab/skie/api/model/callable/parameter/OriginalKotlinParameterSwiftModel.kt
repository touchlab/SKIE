package co.touchlab.skie.api.model.callable.parameter

import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel

class OriginalKotlinParameterSwiftModel(
    delegate: KotlinParameterSwiftModel,
) : KotlinParameterSwiftModel by delegate {

    override val argumentLabel: String = delegate.argumentLabel

    override val isChanged: Boolean = false
}
