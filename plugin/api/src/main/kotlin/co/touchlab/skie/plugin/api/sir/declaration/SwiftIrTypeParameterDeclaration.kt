package co.touchlab.skie.plugin.api.sir.declaration

import io.outfoxx.swiftpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor

sealed interface SwiftIrTypeParameterDeclaration: SwiftIrDeclaration {
    val name: String
    val bounds: List<SwiftIrExtensibleDeclaration>

    fun toInternalSwiftPoetVariable() = TypeVariableName.typeVariable(
        name,
        bounds.map { TypeVariableName.Bound(it.internalName.toSwiftPoetName()) },
    )

    // fun toSwiftPoetVariable(): TypeVariableName = TypeVariableName.typeVariable(
    //     name,
    //     bounds.map { TypeVariableName.Bound(it.toInternalSwiftPoetName()) },
    // )

    class KotlinTypeParameter(
        val descriptor: TypeParameterDescriptor,
        override val name: String,
        override val bounds: List<SwiftIrExtensibleDeclaration>,
    ): SwiftIrTypeParameterDeclaration

    class SwiftTypeParameter(
        override val name: String,
        override val bounds: List<SwiftIrExtensibleDeclaration>,
    ): SwiftIrTypeParameterDeclaration
}
