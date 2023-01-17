package co.touchlab.skie.api.model.parameter

import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel

data class OriginalKotlinParameterSwiftModel(
    override val origin: KotlinParameterSwiftModel.Origin,
    override val argumentLabel: String,
    override val parameterName: String,
) : KotlinParameterSwiftModel {

    override val original: KotlinParameterSwiftModel = this

    override val isChanged: Boolean = false
}
