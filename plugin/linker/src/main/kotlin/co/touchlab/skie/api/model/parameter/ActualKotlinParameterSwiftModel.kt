package co.touchlab.skie.api.model.parameter

import co.touchlab.skie.plugin.api.model.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.parameter.MutableKotlinParameterSwiftModel

data class ActualKotlinParameterSwiftModel(override var argumentLabel: String) : MutableKotlinParameterSwiftModel {

    override val original: KotlinParameterSwiftModel = OriginalKotlinParameterSwiftModel(argumentLabel)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel
}
