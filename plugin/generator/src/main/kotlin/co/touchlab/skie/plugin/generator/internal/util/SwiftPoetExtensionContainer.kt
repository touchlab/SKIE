package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.typeAliasSpec
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

internal interface SwiftPoetExtensionContainer {

    val DeclarationDescriptor.kotlinName: String
        get() = this.fqNameSafe.asString()

    val ClassDescriptor.swiftNameWithTypeParameters: TypeName
        get() = this.typeAliasSpec.withTypeParameters(this)

    fun DeclaredTypeName.withTypeParameters(declaration: ClassDescriptor): TypeName =
        this.withTypeParameters(declaration.swiftTypeVariablesNames)

    fun DeclaredTypeName.withTypeParameters(typeParameters: List<TypeName>): TypeName =
        if (typeParameters.isNotEmpty()) {
            this.parameterizedBy(*typeParameters.toTypedArray())
        } else {
            this
        }

    val ClassDescriptor.swiftTypeVariablesNames: List<TypeVariableName>
        get() = if (this.kind.isInterface) {
            emptyList()
        } else {
            this.declaredTypeParameters.map { it.swiftName }
        }

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

    fun SkieModule.generateCode(
        declaration: DeclarationDescriptor,
        codeBuilder: context(SwiftPoetScope) FileSpec.Builder.() -> Unit,
    ) {
        this.file(declaration.kotlinName, codeBuilder)
    }

    companion object {

        val TYPE_VARIABLE_BASE_BOUND_NAME: DeclaredTypeName =
            DeclaredTypeName.typeName("Swift.AnyObject")
    }
}
