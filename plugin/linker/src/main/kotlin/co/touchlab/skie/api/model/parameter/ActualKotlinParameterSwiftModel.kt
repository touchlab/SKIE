package co.touchlab.skie.api.model.parameter

import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.MutableKotlinParameterSwiftModel

data class ActualKotlinParameterSwiftModel(
    override val origin: KotlinParameterSwiftModel.Origin,
    override var argumentLabel: String,
    override val parameterName: String,
) : MutableKotlinParameterSwiftModel {

    override val original: KotlinParameterSwiftModel = OriginalKotlinParameterSwiftModel(origin, argumentLabel, parameterName)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel
}
