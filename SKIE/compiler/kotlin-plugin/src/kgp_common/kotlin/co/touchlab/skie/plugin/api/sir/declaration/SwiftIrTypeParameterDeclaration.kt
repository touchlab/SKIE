package co.touchlab.skie.plugin.api.sir.declaration

import io.outfoxx.swiftpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

sealed interface SwiftIrTypeParameterDeclaration : SwiftIrDeclaration {

    val name: String
    val bounds: List<SwiftIrExtensibleDeclaration>

    fun toInternalSwiftPoetVariable() = TypeVariableName.typeVariable(
        name,
        bounds.map { TypeVariableName.Bound(it.internalName.toSwiftPoetName()) },
    )

    class KotlinTypeParameter(
        val descriptor: TypeParameterDescriptor,
        override val name: String,
        override val bounds: List<SwiftIrExtensibleDeclaration>,
    ) : SwiftIrTypeParameterDeclaration {

        override fun toString(): String = "type parameter: $name : ${bounds.joinToString("&")}>"
    }

    class SwiftTypeParameter(
        override val name: String,
        override val bounds: List<SwiftIrExtensibleDeclaration>,
    ) : SwiftIrTypeParameterDeclaration {

        override fun toString(): String = "type parameter: $name : ${bounds.joinToString("&")}>"
    }
}
