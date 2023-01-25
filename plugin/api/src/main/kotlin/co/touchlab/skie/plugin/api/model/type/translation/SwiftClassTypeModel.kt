package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftClassTypeModel(
    val className: String,
    val typeArguments: List<SwiftNonNullReferenceTypeModel> = emptyList(),
): SwiftNonNullReferenceTypeModel {
    override val stableFqName: String
        get() = if (typeArguments.isEmpty()) {
            className
        } else {
            "$className<${typeArguments.joinToString { it.stableFqName }}>"
        }
}
