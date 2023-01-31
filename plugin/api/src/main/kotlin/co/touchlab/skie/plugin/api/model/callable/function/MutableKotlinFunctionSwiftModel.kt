package co.touchlab.skie.plugin.api.model.callable.function

import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel, MutableKotlinDirectlyCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>

    override val parameters: List<MutableKotlinParameterSwiftModel>
}
