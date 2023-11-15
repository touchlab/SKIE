package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.oir.element.toTypeParameterUsage
import co.touchlab.skie.oir.type.TypeParameterUsageOirType
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface OirTypeParameterScope {

    val parent: OirTypeParameterScope?

    fun getTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): TypeParameterUsageOirType? =
        parent?.getTypeParameterUsage(typeParameterDescriptor)

    fun wasTypeAlreadyVisited(type: KotlinType): Boolean =
        parent?.wasTypeAlreadyVisited(type) ?: false

    fun deriveFor(type: KotlinType): OirTypeParameterTypeScope? =
        OirTypeParameterTypeScope(this, type)

    fun deriveFor(kirClass: KirClass): OirTypeParameterScope =
        when (kirClass.kind) {
            KirClass.Kind.Class -> OirTypeParameterClassScope(this, kirClass.typeParameters)
            else -> this
        }
}

object OirTypeParameterRootScope : OirTypeParameterScope {

    override val parent: OirTypeParameterScope? = null
}

class OirTypeParameterClassScope(
    override val parent: OirTypeParameterScope,
    private val typeParameters: List<KirTypeParameter>,
) : OirTypeParameterScope {

    override fun getTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): TypeParameterUsageOirType? {
        if (typeParameterDescriptor == null) {
            return null
        }

        return typeParameters
            .firstOrNull {
                it.descriptor == typeParameterDescriptor || (it.descriptor.isCapturedFromOuterDeclaration && it.descriptor.original == typeParameterDescriptor)
            }
            ?.oirTypeParameter
            ?.toTypeParameterUsage()
    }
}

class OirTypeParameterTypeScope private constructor(
    override val parent: OirTypeParameterScope,
    private val type: KotlinType,
) : OirTypeParameterScope {

    override fun wasTypeAlreadyVisited(type: KotlinType): Boolean =
        type == this.type || super.wasTypeAlreadyVisited(type)

    companion object {

        operator fun invoke(parent: OirTypeParameterScope, type: KotlinType): OirTypeParameterTypeScope? =
            if (parent.wasTypeAlreadyVisited(type)) {
                null
            } else {
                OirTypeParameterTypeScope(parent, type)
            }
    }
}

val KirClass.typeParameterScope: OirTypeParameterScope
    get() = OirTypeParameterRootScope.deriveFor(this)
