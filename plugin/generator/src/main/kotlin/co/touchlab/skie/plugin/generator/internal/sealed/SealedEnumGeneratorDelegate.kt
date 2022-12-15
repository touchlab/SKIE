package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SwiftPoetScope
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedEnumGeneratorDelegate(
    override val configuration: Configuration,
) : SealedGeneratorExtensionContainer {

    private val enumName = "Enum"

    context(SwiftPoetScope)
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

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addSealedEnumCases(declaration: ClassDescriptor): TypeSpec.Builder {
        declaration.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    with(sealedSubclass) {
                        swiftNameWithTypeParametersForSealedCase(declaration)
                    },
                )
            }

        if (declaration.hasElseCase) {
            addEnumCase(declaration.elseCaseName)
        }

        return this
    }
}
