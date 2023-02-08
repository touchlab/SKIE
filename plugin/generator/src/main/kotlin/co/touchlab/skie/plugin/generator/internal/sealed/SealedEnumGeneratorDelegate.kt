package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec

internal class SealedEnumGeneratorDelegate(
    override val configuration: Configuration,
) : SealedGeneratorExtensionContainer {

    private val enumName = "Enum"

    fun generate(swiftModel: KotlinClassSwiftModel, classNamespace: DeclaredTypeName, fileBuilder: FileSpec.Builder): TypeName {
        fileBuilder.addExtension(
            ExtensionSpec.builder(classNamespace)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(enumName)
                        .addAttribute("frozen")
                        .addTypeVariables(swiftModel.swiftTypeVariablesNames)
                        .addSealedEnumCases(swiftModel)
                        .build()
                )
                .build()
        )

        return classNamespace.nestedType(enumName).withTypeParameters(swiftModel)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(swiftModel: KotlinClassSwiftModel): TypeSpec.Builder {
        swiftModel.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    with(sealedSubclass) {
                        swiftNameWithTypeParametersForSealedCase(swiftModel)
                    },
                )
            }

        if (swiftModel.hasElseCase) {
            addEnumCase(swiftModel.elseCaseName)
        }

        return this
    }
}
