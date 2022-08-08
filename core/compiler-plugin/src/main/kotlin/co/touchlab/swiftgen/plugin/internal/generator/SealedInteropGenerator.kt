package co.touchlab.swiftgen.plugin.internal.generator

import co.touchlab.swiftgen.plugin.internal.kotlinName
import co.touchlab.swiftgen.plugin.internal.swiftName
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

internal class SealedInteropGenerator(
    output: SwiftPackModuleBuilder,
    moduleFragment: IrModuleFragment,
) : BaseGenerator<IrClass>(output, moduleFragment) {

    override fun generate(declaration: IrClass) {
        if (declaration.sealedSubclasses.isEmpty()) {
            return
        }

        generateCode(declaration) {
            val classNamespace = addNamespace(swiftGenNamespace, declaration.kotlinName)

            val enumType = addSealedEnum(declaration, classNamespace)

            addExhaustivelyFunction(declaration, enumType)
        }
    }

    private fun FileSpec.Builder.addSealedEnum(
        declaration: IrClass,
        classNamespace: DeclaredTypeName,
    ): DeclaredTypeName {
        val enumName = "Enum"

        addExtension(
            ExtensionSpec.builder(classNamespace)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(enumName)
                        .addAttribute("frozen")
                        .addSealedEnumCases(declaration)
                        .build()
                )
                .build()
        )

        return classNamespace.nestedType(enumName)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(declaration: IrClass): TypeSpec.Builder {
        declaration.sealedSubclasses.forEach { sealedSubclass ->
            addEnumCase(
                sealedSubclass.enumCaseName,
                sealedSubclass.owner.swiftName,
            )
        }

        return this
    }

    private fun FileSpec.Builder.addExhaustivelyFunction(declaration: IrClass, enumType: DeclaredTypeName) {
        addFunction(
            FunctionSpec.builder("exhaustively")
                .addModifiers(Modifier.PUBLIC)
                .addParameter("_", "self", declaration.swiftName)
                .returns(enumType)
                .addExhaustivelyFunctionBody(declaration, enumType)
                .build()
        )
    }

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: IrClass,
        enumType: DeclaredTypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .apply {
                var isFirst = true

                declaration.sealedSubclasses.forEach { sealedSubclassSymbol ->
                    val condition = "let v = self as? ${sealedSubclassSymbol.owner.swiftName.canonicalName}"

                    if (isFirst) {
                        isFirst = false

                        beginControlFlow("if", condition)
                    } else {
                        nextControlFlow("else if", condition)
                    }

                    add("return ${enumType.canonicalName}.${sealedSubclassSymbol.enumCaseName}(v)\n")
                }

                nextControlFlow("else")
                add(
                    "fatalError(\"Unknown subtype. " +
                            "This error should not happen under normal circumstances since Self is sealed.\")\n"
                )
                endControlFlow("else")
            }
            .build()
    )

    private val IrClassSymbol.enumCaseName: String
        get() = this.owner.name.identifier
}