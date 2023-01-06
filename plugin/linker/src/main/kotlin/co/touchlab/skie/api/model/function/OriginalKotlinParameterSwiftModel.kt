package co.touchlab.skie.api.model.function

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel

data class OriginalKotlinParameterSwiftModel(override val argumentLabel: String) : KotlinFunctionSwiftModel.Parameter {

    override val original: KotlinFunctionSwiftModel.Parameter = this

    override val isChanged: Boolean = false
}
