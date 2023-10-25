package co.touchlab.skie.phases.features.flow

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

enum class FlowMappingStrategy {
    Full, TypeArgumentsOnly, None;

    private val incompatibleTypeFqNames = listOf(
        "kotlinx.coroutines.flow.Flow",
        "kotlin.collections.List",
        "kotlin.collections.Map",
    )

    fun limitedToTypeArguments(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> TypeArgumentsOnly
            None -> None
        }

    fun forTypeArgumentsOf(kotlinType: KotlinType): FlowMappingStrategy =
        if (kotlinType.isIncompatibleWithSkieFlows) limitedToTypeArguments() else forTypeArgumentsOfCompatibleType()

    private val KotlinType.isIncompatibleWithSkieFlows: Boolean
        get() = (listOf(this) + supertypes()).any {
            (it.constructor.declarationDescriptor as? ClassDescriptor)?.fqNameSafe?.asString() in incompatibleTypeFqNames
        }

    private fun forTypeArgumentsOfCompatibleType(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> Full
            None -> None
        }
}
