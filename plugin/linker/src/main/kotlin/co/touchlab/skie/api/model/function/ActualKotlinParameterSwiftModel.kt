package co.touchlab.skie.api.model.function

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel

data class ActualKotlinParameterSwiftModel(override var argumentLabel: String) : MutableKotlinFunctionSwiftModel.MutableParameter {

    override val original: KotlinFunctionSwiftModel.Parameter = OriginalKotlinParameterSwiftModel(argumentLabel)

    override val isChanged: Boolean
        get() = argumentLabel != original.argumentLabel
}
