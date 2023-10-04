package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel, MutableKotlinDirectlyCallableMemberSwiftModel {

    override val asyncSwiftModelOrNull: MutableKotlinFunctionSwiftModel?

    override var bridgedSirFunction: SirFunction?

    override var bridgedSirCallableDeclaration: SirCallableDeclaration?

    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel>
}
