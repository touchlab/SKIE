package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedEnumGeneratorDelegate(
    override val configuration: Configuration,
    override val swiftPackModuleBuilder: SwiftPackModuleBuilder,
) : SealedGeneratorExtensionContainer {

    private val enumName = "Enum"

    fun generate(declaration: ClassDescriptor, classNamespace: DeclaredTypeName, fileBuilder: FileSpec.Builder): TypeName {
        fileBuilder.addExtension(
            ExtensionSpec.builder(classNamespace)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(enumName)
                        .addAttribute("frozen")
                        .addTypeVariables(declaration.swiftTypeVariablesNames)
                        .addSealedEnumCases(declaration)
                        .build()
                )
                .build()
        )

        return classNamespace.nestedType(enumName).withTypeParameters(declaration)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(declaration: ClassDescriptor): TypeSpec.Builder {
        declaration.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    sealedSubclass.swiftNameWithTypeParametersForSealedCase(declaration),
                )
            }

        if (declaration.hasElseCase) {
            addEnumCase(declaration.elseCaseName)
        }

        return this
    }
}