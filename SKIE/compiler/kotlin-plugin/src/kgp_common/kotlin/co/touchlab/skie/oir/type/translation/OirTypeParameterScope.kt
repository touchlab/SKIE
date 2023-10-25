package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.oir.element.toTypeParameterUsage
import co.touchlab.skie.oir.type.TypeParameterUsageOirType
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

class OirTypeParameterScope(private val typeParameters: List<KirTypeParameter>) {

    fun getTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): TypeParameterUsageOirType? {
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

    companion object {

        val None = OirTypeParameterScope(emptyList())
    }
}

val KirClass.typeParameterScope: OirTypeParameterScope
    get() = if (this.kind == KirClass.Kind.Class) {
        OirTypeParameterScope(this.typeParameters)
    } else {
        OirTypeParameterScope.None
    }
