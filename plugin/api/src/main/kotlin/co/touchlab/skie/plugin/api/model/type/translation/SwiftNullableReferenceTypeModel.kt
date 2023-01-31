package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftNullableReferenceTypeModel(
    val nonNullType: SwiftNonNullReferenceTypeModel,
    val isNullableResult: Boolean = false,
) : SwiftReferenceTypeModel {

    override val stableFqName: String
        get() = if (nonNullType is SwiftLambdaTypeModel) {
            "(${nonNullType.stableFqName})?"
        } else {
            "${nonNullType.stableFqName}?"
        }
}
