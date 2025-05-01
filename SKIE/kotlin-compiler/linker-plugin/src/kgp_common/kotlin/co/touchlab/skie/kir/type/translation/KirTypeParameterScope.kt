package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.element.toTypeParameterUsage
import co.touchlab.skie.kir.type.TypeParameterUsageKirType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface KirTypeParameterScope {

    val parent: KirTypeParameterScope?

    context(DescriptorKirProvider)
    fun getTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): TypeParameterUsageKirType? =
        parent?.getTypeParameterUsage(typeParameterDescriptor)

    fun wasTypeAlreadyVisited(type: KotlinType): Boolean = parent?.wasTypeAlreadyVisited(type) ?: false
}

inline fun <T : Any> KirTypeParameterScope.withTypeParameterScopeFor(type: KotlinType, action: KirTypeParameterScope.() -> T): T? =
    KirTypeParameterTypeScope(this, type)?.let(action)

@OptIn(ExperimentalContracts::class)
inline fun KirClass.withTypeParameterScope(action: KirTypeParameterScope.() -> Unit) {
    contract {
        callsInPlace(action, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }

    val scope = when (kind) {
        KirClass.Kind.Class -> KirTypeParameterClassScope(KirTypeParameterRootScope, typeParameters)
        else -> KirTypeParameterRootScope
    }

    scope.action()
}

object KirTypeParameterRootScope : KirTypeParameterScope {

    override val parent: KirTypeParameterScope? = null
}

class KirTypeParameterClassScope(override val parent: KirTypeParameterScope, private val typeParameters: List<KirTypeParameter>) :
    KirTypeParameterScope {

    context(DescriptorKirProvider)
    override fun getTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): TypeParameterUsageKirType? {
        if (typeParameterDescriptor == null) {
            return null
        }

        return typeParameters
            .firstOrNull {
                val descriptor = getTypeParameterDescriptor(it)

                descriptor == typeParameterDescriptor ||
                    (descriptor.isCapturedFromOuterDeclaration && descriptor.original == typeParameterDescriptor)
            }
            ?.toTypeParameterUsage()
    }
}

class KirTypeParameterTypeScope private constructor(override val parent: KirTypeParameterScope, private val type: KotlinType) :
    KirTypeParameterScope {

    override fun wasTypeAlreadyVisited(type: KotlinType): Boolean = type == this.type || super.wasTypeAlreadyVisited(type)

    companion object {

        operator fun invoke(parent: KirTypeParameterScope, type: KotlinType): KirTypeParameterTypeScope? =
            if (parent.wasTypeAlreadyVisited(type)) {
                null
            } else {
                KirTypeParameterTypeScope(parent, type)
            }
    }
}
