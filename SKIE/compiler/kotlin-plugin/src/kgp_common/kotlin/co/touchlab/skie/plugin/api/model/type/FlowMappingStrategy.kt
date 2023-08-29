package co.touchlab.skie.plugin.api.model.type

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

    fun forTypeArgumentsOf(kotlinType: KotlinType): FlowMappingStrategy =
        if (kotlinType.isIncompatibleWithSkieFlows) forTypeArgumentsOfIncompatibleType() else forTypeArgumentsOfCompatibleType()

    private val KotlinType.isIncompatibleWithSkieFlows: Boolean
        get() = (listOf(this) + supertypes()).any {
            (it.constructor.declarationDescriptor as? ClassDescriptor)?.fqNameSafe?.asString() in incompatibleTypeFqNames
        }

    private fun forTypeArgumentsOfIncompatibleType(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> TypeArgumentsOnly
            None -> None
        }

    private fun forTypeArgumentsOfCompatibleType(): FlowMappingStrategy =
        when (this) {
            Full, TypeArgumentsOnly -> Full
            None -> None
        }
}
