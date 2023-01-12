package co.touchlab.skie.api.model.parameter

import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel

data class OriginalKotlinParameterSwiftModel(override val argumentLabel: String) : KotlinParameterSwiftModel {

    override val original: KotlinParameterSwiftModel = this

    override val isChanged: Boolean = false
}
