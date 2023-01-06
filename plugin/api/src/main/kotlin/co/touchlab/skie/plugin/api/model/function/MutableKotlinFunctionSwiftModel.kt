package co.touchlab.skie.plugin.api.model.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel

interface MutableKotlinFunctionSwiftModel : KotlinFunctionSwiftModel {

    override var visibility: SwiftModelVisibility

    override val receiver: MutableKotlinTypeSwiftModel

    override var identifier: String

    override val parameters: List<MutableParameter>

    interface MutableParameter : KotlinFunctionSwiftModel.Parameter {

        override var argumentLabel: String
    }
}
