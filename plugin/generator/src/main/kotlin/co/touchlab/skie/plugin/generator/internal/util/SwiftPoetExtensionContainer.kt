package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.module.stableSpec
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

    // Cannot use context because of bug in implementation
    fun SwiftModelScope.swiftNameWithTypeParameters(declaration: ClassDescriptor): TypeName =
        declaration.stableSpec.withTypeParameters(declaration)

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
        codeBuilder: context(SwiftModelScope) FileSpec.Builder.() -> Unit,
    ) {
        this.file(declaration.kotlinName, contents = codeBuilder)
    }

    fun SkieModule.generateCode(
        swiftModel: KotlinCallableMemberSwiftModel,
        codeBuilder: context(SwiftModelScope) FileSpec.Builder.() -> Unit,
    ) {
        generateCode(swiftModel.descriptor, codeBuilder)
    }

    fun SkieModule.generateCode(
        swiftModel: KotlinClassSwiftModel,
        codeBuilder: context(SwiftModelScope) FileSpec.Builder.() -> Unit,
    ) {
        generateCode(swiftModel.classDescriptor, codeBuilder)
    }

    companion object {

        val TYPE_VARIABLE_BASE_BOUND_NAME: DeclaredTypeName =
            DeclaredTypeName.typeName("Swift.AnyObject")
    }
}
