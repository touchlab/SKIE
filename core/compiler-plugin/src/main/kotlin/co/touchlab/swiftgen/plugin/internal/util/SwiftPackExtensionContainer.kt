package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal interface SwiftPackExtensionContainer {

    val swiftPackModuleBuilder: SwiftPackModuleBuilder

    @Deprecated("Descriptors")
    val IrClass.kotlinName: String
        get() = this.kotlinFqName.asString()

    val DeclarationDescriptor.kotlinName: String
        get() = this.fqNameSafe.asString()

    @Deprecated("Descriptors")
    val IrClass.swiftName: DeclaredTypeName
        get() = with(swiftPackModuleBuilder) { this@swiftName.reference().swiftReference() }

    val ClassDescriptor.swiftName: DeclaredTypeName
        get() = with(swiftPackModuleBuilder) { this@swiftName.reference().swiftReference() }

    @Deprecated("Descriptors")
    val IrClass.swiftNameWithTypeParameters: TypeName
        get() = this.swiftName.withTypeParameters(this)

    val ClassDescriptor.swiftNameWithTypeParameters: TypeName
        get() = this.swiftName.withTypeParameters(this)

    @Deprecated("Descriptors")
    fun DeclaredTypeName.withTypeParameters(declaration: IrClass): TypeName =
        this.withTypeParameters(declaration.typeVariablesNames)

    fun DeclaredTypeName.withTypeParameters(declaration: ClassDescriptor): TypeName =
        this.withTypeParameters(declaration.swiftTypeVariablesNames)

    fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    @Deprecated("Descriptors")
    val IrClass.typeVariablesNames: List<TypeVariableName>
        get() = if (this.isInterface) {
            emptyList()
        } else {
            this.typeParameters.map { it.swiftName }
        }

    val ClassDescriptor.swiftTypeVariablesNames: List<TypeVariableName>
        get() = if (this.kind.isInterface) {
            emptyList()
        } else {
            this.declaredTypeParameters.map { it.swiftName }
        }

    @Deprecated("Descriptors")
    val IrTypeParameter.swiftName: TypeVariableName
        get() = TypeVariableName.typeVariable(this.name.identifier)
            .withBounds(TypeVariableName.bound(TYPE_VARIABLE_BASE_BOUND_NAME))

    val TypeParameterDescriptor.swiftName: TypeVariableName
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