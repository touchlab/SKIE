package co.touchlab.swiftgen.plugin

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.api.kotlin
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class SwiftGenVisitor(private val output: SwiftPackModuleBuilder) : IrElementVisitor<Unit, Unit> {

    override fun visitElement(element: IrElement, data: Unit) {
        element.acceptChildren(this, Unit)
    }

    override fun visitClass(declaration: IrClass, data: Unit) {
        super.visitClass(declaration, data)

        val sealedSubclasses = declaration.sealedSubclasses

        if (sealedSubclasses.isNotEmpty()) {
            with(output) {
                val kotlinName = declaration.kotlinFqName.asString()

                file(kotlinName) {
                    val swiftName = DeclaredTypeName.kotlin(kotlinName)

                    val enum = TypeSpec.enumBuilder("Enum")
                        .addAttribute("frozen")
                        .apply {

                            sealedSubclasses.forEach { sealedSubclass ->
                                addEnumCase(
                                    sealedSubclass.owner.name.identifier,
                                    DeclaredTypeName.kotlin(sealedSubclass.owner.kotlinFqName.asString()),
                                )
                            }
                        }
                        .build()

                    val enumName = swiftName.nestedType("Enum").canonicalName

                    addExtension(
                        ExtensionSpec.builder(swiftName)
                            .addModifiers(Modifier.PUBLIC)
                            .addType(enum)
                            .addFunction(
                                FunctionSpec.builder("exhaustively")
                                    .returns(DeclaredTypeName.qualifiedTypeName(enumName))
                                    .addCode(
                                        CodeBlock.builder()
                                            .appendExhaustivelyFunctionBody(enumName, sealedSubclasses)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                }
            }
        }
    }

    private fun CodeBlock.Builder.appendExhaustivelyFunctionBody(
        enumName: String,
        sealedSubclasses: List<IrClassSymbol>,
    ): CodeBlock.Builder {
        var isFirst = true

        sealedSubclasses.forEach { sealedSubclass ->
            val swiftName = DeclaredTypeName.kotlin(sealedSubclass.owner.kotlinFqName.asString()).canonicalName
            val condition = "let v = self as? $swiftName"

            if (isFirst) {
                isFirst = false
                
                beginControlFlow("if", condition)
            } else {
                nextControlFlow("else if", condition)
            }

            add("return ${enumName}.${sealedSubclass.owner.name.identifier}(v)\n")
        }

        nextControlFlow("else")
        add(
            "fatalError(\"Unknown subtype. This error should not happen under normal circumstances since Self is sealed.\")\n"
        )
        endControlFlow("else")

        return this
    }
}