package co.touchlab.skie.kir.type

data class UnresolvedFlowKirType(
    val flowType: SupportedFlow.Variant,
    val evaluateFlowTypeArgument: () -> NonNullReferenceKirType,
) : DeclarationBackedKirType() {

    override fun asDeclaredKirTypeOrError(): DeclaredKirType =
        error("${this::class.simpleName} must be resolved before translation to Oir.")
}
