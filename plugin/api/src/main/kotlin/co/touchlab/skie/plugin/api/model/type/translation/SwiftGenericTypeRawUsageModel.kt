package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftGenericTypeRawUsageModel(
    override val typeName: String,
): SwiftGenericTypeUsageModel {
    override val stableFqName: String
        get() = TODO("Not yet implemented")
}
