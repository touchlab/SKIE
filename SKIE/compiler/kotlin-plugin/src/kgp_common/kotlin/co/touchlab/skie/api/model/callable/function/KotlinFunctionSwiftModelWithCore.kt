package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel

internal interface KotlinFunctionSwiftModelWithCore : MutableKotlinFunctionSwiftModel {

    val core: KotlinFunctionSwiftModelCore

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModelWithCore>
}
