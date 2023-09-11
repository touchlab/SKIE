package co.touchlab.skie.plugin.api.util.flow

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.sir.element.SirClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

enum class SupportedFlow(private val directParent: SupportedFlow?) {
    Flow(null),
    SharedFlow(Flow),
    MutableSharedFlow(SharedFlow),
    StateFlow(SharedFlow),
    MutableStateFlow(StateFlow);

    val coroutinesFlowFqName: String = "kotlinx.coroutines.flow.${name}"

    val requiredVariant: Variant.Required = Variant.Required(this)
    val optionalVariant: Variant.Optional = Variant.Optional(this)

    val variants: List<Variant> = listOf(requiredVariant, optionalVariant)

    sealed interface Variant {

        val owner: SupportedFlow

        context(SwiftModelScope)
        fun swiftFlowClass(): SirClass

        val kotlinFlowFqName: String

        context (SwiftModelScope)
        val kotlinFlowModel: KotlinClassSwiftModel
            get() = referenceClass(kotlinFlowFqName)

        context (MutableSwiftModelScope)
        val kotlinFlowModel: MutableKotlinClassSwiftModel
            get() = referenceClass(kotlinFlowFqName)

        fun isCastableTo(variant: Variant): Boolean

        class Required(override val owner: SupportedFlow) : Variant {

            override val kotlinFlowFqName: String = "co.touchlab.skie.runtime.coroutines.flow.SkieKotlin${owner.name}"

            context(SwiftModelScope)
            override fun swiftFlowClass(): SirClass =
                when (owner) {
                    Flow -> sirBuiltins.Skie.SkieSwiftFlow
                    SharedFlow -> sirBuiltins.Skie.SkieSwiftSharedFlow
                    MutableSharedFlow -> sirBuiltins.Skie.SkieSwiftMutableSharedFlow
                    StateFlow -> sirBuiltins.Skie.SkieSwiftStateFlow
                    MutableStateFlow -> sirBuiltins.Skie.SkieSwiftMutableStateFlow
                }

            override fun isCastableTo(variant: Variant): Boolean {
                return owner.isSelfOrChildOf(variant.owner)
            }
        }

        class Optional(override val owner: SupportedFlow) : Variant {

            override val kotlinFlowFqName: String = "co.touchlab.skie.runtime.coroutines.flow.SkieKotlinOptional${owner.name}"

            context(SwiftModelScope)
            override fun swiftFlowClass(): SirClass =
                when (owner) {
                    Flow -> sirBuiltins.Skie.SkieSwiftOptionalFlow
                    SharedFlow -> sirBuiltins.Skie.SkieSwiftOptionalSharedFlow
                    MutableSharedFlow -> sirBuiltins.Skie.SkieSwiftOptionalMutableSharedFlow
                    StateFlow -> sirBuiltins.Skie.SkieSwiftOptionalStateFlow
                    MutableStateFlow -> sirBuiltins.Skie.SkieSwiftOptionalMutableStateFlow
                }

            override fun isCastableTo(variant: Variant): Boolean {
                if (variant is Required) return false

                return owner.isSelfOrChildOf(variant.owner)
            }
        }
    }

    fun isSelfOrChildOf(flow: SupportedFlow): Boolean =
        this == flow || (directParent?.isSelfOrChildOf(flow) ?: false)

    companion object {

        fun from(classDescriptor: ClassDescriptor): SupportedFlow? {
            val classFqName = classDescriptor.fqNameSafe.asString()

            return values().firstOrNull { it.coroutinesFlowFqName == classFqName }
        }

        fun from(type: KotlinType): SupportedFlow? =
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { from(it) }
    }
}
