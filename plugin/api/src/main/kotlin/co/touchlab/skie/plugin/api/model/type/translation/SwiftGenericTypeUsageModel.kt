package co.touchlab.skie.plugin.api.model.type.translation

sealed interface SwiftGenericTypeUsageModel: SwiftNonNullReferenceTypeModel {
    val typeName: String
}
