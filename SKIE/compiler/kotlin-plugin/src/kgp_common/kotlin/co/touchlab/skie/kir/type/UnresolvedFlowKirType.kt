package co.touchlab.skie.kir.type

import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.phases.features.flow.SupportedFlow

data class UnresolvedFlowKirType(
    val flowType: SupportedFlow.Variant,
    val evaluateFlowTypeArgument: KirTypeTranslator.() -> NonNullReferenceKirType,
) : DeclarationBackedKirType()
