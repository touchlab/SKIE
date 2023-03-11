package co.touchlab.skie.plugin.api.util.flow

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeParameterDeclaration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

enum class SupportedFlow(val directParent: SupportedFlow?) {
    Flow(null),
    SharedFlow(Flow);

    val coroutinesFlowFqName: String = "kotlinx.coroutines.flow.${name}"

    val requiredVariant: Variant.Required = Variant.Required(this)
    val optionalVariant: Variant.Optional = Variant.Optional(this)

    val variants: List<Variant> = listOf(requiredVariant, optionalVariant)

    context (SwiftModelScope)
    val coroutinesFlowModel: KotlinClassSwiftModel
        get() = referenceClass(coroutinesFlowFqName)

    sealed interface Variant {

        val owner: SupportedFlow

        val swiftFlowDeclaration: SwiftIrTypeDeclaration.Local.SwiftType

        val kotlinFlowFqName: String

        // val swiftFlowFqName: String

        context (SwiftModelScope)
        val kotlinFlowModel: KotlinClassSwiftModel
            get() = referenceClass(kotlinFlowFqName)

        context (MutableSwiftModelScope)
        val kotlinFlowModel: MutableKotlinClassSwiftModel
            get() = referenceClass(kotlinFlowFqName)

        fun isCastableTo(variant: Variant): Boolean

        class Required(override val owner: SupportedFlow) : Variant {

            override val kotlinFlowFqName: String = "co.touchlab.skie.runtime.coroutines.flow.SkieKotlin${owner.name}"

            override val swiftFlowDeclaration: SwiftIrTypeDeclaration.Local.SwiftType = SwiftIrTypeDeclaration.Local.SwiftType(
                swiftName = "SkieSwift${owner.name}",
                typeParameters = listOf(
                    SwiftIrTypeParameterDeclaration.SwiftTypeParameter(name = "T", bounds = listOf(BuiltinDeclarations.Swift.AnyObject)),
                ),
                superTypes = listOf(
                    // TODO: Verify if this is enough, or we should have a separate `SwiftIrReferenceDeclaration` for classes
                    // This is how we tell when it's a class, or a reference protocol
                    BuiltinDeclarations.Swift.AnyObject,
                    // TODO: We don't use these supertypes yet, will we need them?
                    // "_Concurrency.AsyncSequence", "Swift._ObjectiveCBridgeable"
                ),
            )

            override fun isCastableTo(variant: Variant): Boolean {
                return owner.isSelfOrChildOf(variant.owner)
            }
        }

        class Optional(override val owner: SupportedFlow) : Variant {

            override val kotlinFlowFqName: String = "co.touchlab.skie.runtime.coroutines.flow.SkieKotlinOptional${owner.name}"

            override val swiftFlowDeclaration: SwiftIrTypeDeclaration.Local.SwiftType = SwiftIrTypeDeclaration.Local.SwiftType(
                swiftName = "SkieSwiftOptional${owner.name}",
                typeParameters = listOf(
                    SwiftIrTypeParameterDeclaration.SwiftTypeParameter(name = "T", bounds = listOf(BuiltinDeclarations.Swift.AnyObject)),
                ),
                superTypes = listOf(
                    // TODO: Verify if this is enough, or we should have a separate `SwiftIrReferenceDeclaration` for classes
                    // This is how we tell when it's a class, or a reference protocol
                    BuiltinDeclarations.Swift.AnyObject,
                    // TODO: We don't use these supertypes yet, will we need them?
                    // "_Concurrency.AsyncSequence", "Swift._ObjectiveCBridgeable"
                ),
            )

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
            classDescriptor.typeConstructor

            return values().firstOrNull { it.coroutinesFlowFqName == classFqName }
        }

        fun from(type: KotlinType): SupportedFlow? =
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { from(it) }
    }
}
