package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.DeclarationBackedKirType
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.kir.type.UnresolvedFlowKirType

enum class FlowMappingStrategy {
    Full, TypeArgumentsOnly, None;

    private val incompatibleTypeFqNames = listOf(
        "kotlinx.coroutines.flow.Flow",
        "kotlin.collections.List",
        "kotlin.collections.Map",
    )

    fun limitFlowMappingToTypeArguments(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> TypeArgumentsOnly
            None -> None
        }

    inline fun <T> KirClass.withFlowMappingForTypeArguments(action: FlowMappingStrategy.() -> T): T =
        flowMappingForTypeArgumentsOf(this).action()

    fun flowMappingForTypeArgumentsOf(kirClass: KirClass): FlowMappingStrategy =
        if (kirClass.isIncompatibleWithSkieFlows) limitFlowMappingToTypeArguments() else forTypeArgumentsOfCompatibleType()

    private val KirClass.isIncompatibleWithSkieFlows: Boolean
        get() = kotlinFqName in incompatibleTypeFqNames || superTypes.any { it.isIncompatibleWithSkieFlows }

    private val DeclarationBackedKirType.isIncompatibleWithSkieFlows: Boolean
        get() = when (this) {
            is DeclaredKirType -> declaration.isIncompatibleWithSkieFlows
            is UnresolvedFlowKirType -> true
        }

    private fun forTypeArgumentsOfCompatibleType(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> Full
            None -> None
        }
}
