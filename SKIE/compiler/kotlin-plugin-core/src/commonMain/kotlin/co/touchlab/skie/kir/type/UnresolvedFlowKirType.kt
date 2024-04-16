package co.touchlab.skie.kir.type

data class UnresolvedFlowKirType(
    val flowType: SupportedFlow.Variant,
    val evaluateFlowTypeArgument: () -> NonNullReferenceKirType,
) : DeclarationBackedKirType()
