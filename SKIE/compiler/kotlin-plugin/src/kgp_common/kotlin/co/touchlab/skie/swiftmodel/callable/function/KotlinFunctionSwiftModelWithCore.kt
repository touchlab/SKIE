package co.touchlab.skie.swiftmodel.callable.function

internal interface KotlinFunctionSwiftModelWithCore : MutableKotlinFunctionSwiftModel {

    val core: KotlinFunctionSwiftModelCore

    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModelWithCore>
}
