package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftLambdaTypeModel(
    val returnType: SwiftTypeModel,
    val parameterTypes: List<SwiftReferenceTypeModel>,
    val isEscaping: Boolean,
): SwiftNonNullReferenceTypeModel {
    override val stableFqName: String
        get() = if (isEscaping) { "@escaping " } else { "" } + "(${parameterTypes.joinToString { it.stableFqName }}) -> ${returnType.stableFqName}"
}
