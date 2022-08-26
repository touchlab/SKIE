package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName

internal interface SwiftPackExtensionContainer {

    val swiftPackModuleBuilder: SwiftPackModuleBuilder

    val IrClass.kotlinName: String
        get() = this.kotlinFqName.asString()

    val IrClass.swiftName: DeclaredTypeName
        get() = with(swiftPackModuleBuilder) { this@swiftName.reference().swiftReference() }

    val IrClass.swiftNameWithTypeParameters: TypeName
        get() = this.swiftName.withTypeParameters(this)

    fun DeclaredTypeName.withTypeParameters(declaration: IrClass): TypeName =
        this.withTypeParameters(declaration.typeVariablesNames)

    fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    val IrClass.typeVariablesNames: List<TypeVariableName>
        get() = if (this.isInterface) {
            emptyList()
        } else {
            this.typeParameters.map { it.swiftName }
        }

    val IrTypeParameter.swiftName: TypeVariableName
        get() = TypeVariableName.typeVariable(this.name.identifier)
            .withBounds(TypeVariableName.bound(TYPE_VARIABLE_BASE_BOUND_NAME))

    val TypeName.canonicalName: String
        get() = when (this) {
            is DeclaredTypeName -> this.canonicalName
            is ParameterizedTypeName -> {
                this.rawType.canonicalName +
                        this.typeArguments.joinToString(", ", prefix = "<", postfix = ">") { it.name }
            }
            else -> error("TypeName $this is not supported.")
        }

    companion object {

        val TYPE_VARIABLE_BASE_BOUND_NAME: DeclaredTypeName =
            DeclaredTypeName.typeName("Swift.AnyObject")
    }
}