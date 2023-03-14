package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.SwiftFqName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec

internal class SealedEnumGeneratorDelegate(
    override val skieContext: SkieContext,
) : SealedGeneratorExtensionContainer {

    private val enumName = "Enum"

    fun generate(swiftModel: KotlinClassSwiftModel, classNamespace: SwiftFqName.Local, fileBuilder: FileSpec.Builder): TypeName {
        fileBuilder.addExtension(
            ExtensionSpec.builder(classNamespace.toSwiftPoetName())
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

        return classNamespace.nested(enumName).toSwiftPoetName().withTypeParameters(swiftModel)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(swiftModel: KotlinClassSwiftModel): TypeSpec.Builder {
        val preferredNamesCollide = swiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide

        swiftModel.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName(preferredNamesCollide),
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
