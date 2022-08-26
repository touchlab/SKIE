package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.typeVariablesNames
import co.touchlab.swiftgen.plugin.internal.util.withTypeParameters
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.declarations.IrClass

internal class SealedEnumGeneratorDelegate(
    override val configuration: SwiftGenConfiguration.SealedInteropDefaults,
) : SealedGeneratorExtensionContainer {

    private val enumName = "Enum"

    fun generate(declaration: IrClass, classNamespace: DeclaredTypeName, fileBuilder: FileSpec.Builder): TypeName {
        fileBuilder.addExtension(
            ExtensionSpec.builder(classNamespace)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(enumName)
                        .addAttribute("frozen")
                        .addTypeVariables(declaration.typeVariablesNames)
                        .addSealedEnumCases(declaration)
                        .build()
                )
                .build()
        )

        return classNamespace.nestedType(enumName).withTypeParameters(declaration)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(declaration: IrClass): TypeSpec.Builder {
        declaration.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    sealedSubclass.owner.swiftNameWithTypeParametersForSealedCase(declaration),
                )
            }

        if (declaration.hasElseCase) {
            addEnumCase(declaration.elseCaseName)
        }

        return this
    }
}