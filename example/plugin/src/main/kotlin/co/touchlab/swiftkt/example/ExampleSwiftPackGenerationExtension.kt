package co.touchlab.swiftkt.example

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.api.buildSwiftPackModule
import co.touchlab.swiftpack.plugin.SwiftPackGenerationExtension
import co.touchlab.swiftpack.spec.KotlinPackageReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.function
import co.touchlab.swiftpack.spec.property
import com.google.auto.service.AutoService
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.getBooleanConstArgument
import org.jetbrains.kotlin.backend.jvm.ir.getStringConstArgument
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

data class TestAnnotation(
    val rename: String?,
    val hide: Boolean,
    val remove: Boolean,
    val invocation: String?,
) {
    companion object {
        val fqName = FqName("co.touchlab.swiftkt.Test")
    }
}
val IrAnnotationContainer.testAnnotation: TestAnnotation?
    get() = this.getAnnotation(TestAnnotation.fqName)?.let {
        TestAnnotation(
            rename = try { it.getStringConstArgument(0) } catch (e: AssertionError) { null },
            hide = try { it.getBooleanConstArgument(1) } catch (e: AssertionError) { false },
            remove = try { it.getBooleanConstArgument(2) } catch (e: AssertionError) { false },
            invocation = try { it.getStringConstArgument(3) } catch (e: AssertionError) { null },
        )
    }

class TestIrGenerator(val moduleBuilder: SwiftPackModuleBuilder, val codeBlocks: MutableList<CodeBlock>): IrElementTransformerVoid() {

    override fun visitClass(declaration: IrClass): IrStatement = with(moduleBuilder) {
        val testAnnotation = declaration.testAnnotation

        if (testAnnotation != null) {
            val ref = declaration.reference().applyTransform {
                if (testAnnotation.rename != null) {
                    rename(testAnnotation.rename)
                }
                if (testAnnotation.hide) {
                    hide()
                }
                if (testAnnotation.remove) {
                    remove()
                }
            }

            val typeSuffix = testAnnotation.invocation ?: ".self"

            codeBlocks.add(
                CodeBlock.of("print(%S, %T%L)", declaration.name.asString(), ref.swiftReference(), typeSuffix)
            )
        }

        return super.visitClass(declaration)
    }

    override fun visitProperty(declaration: IrProperty): IrStatement = with(moduleBuilder) {
        val testAnnotation = declaration.testAnnotation

        if (testAnnotation != null) {
            val ref = declaration.reference().applyTransform {
                if (testAnnotation.rename != null) {
                    rename(testAnnotation.rename)
                }
                if (testAnnotation.hide) {
                    hide()
                }
                if (testAnnotation.remove) {
                    remove()
                }
            }

            when (val parent = ref.parent) {
                is KotlinPackageReference -> codeBlocks.add(
                    CodeBlock.of("print(%S, %N)", declaration.name.asString(), ref.swiftReference())
                )
                is KotlinTypeReference -> codeBlocks.add(
                    CodeBlock.of("print(%S, %T().%N)", declaration.name.asString(), parent.swiftReference(), ref.swiftReference())
                )
            }
        }

        return super.visitProperty(declaration)
    }

    override fun visitFunction(declaration: IrFunction): IrStatement = with(moduleBuilder) {
        val testAnnotation = declaration.testAnnotation

        if (testAnnotation != null) {
            val ref = declaration.reference().applyTransform {
                if (testAnnotation.rename != null) {
                    rename(testAnnotation.rename)
                }
                if (testAnnotation.hide) {
                    hide()
                }
                if (testAnnotation.remove) {
                    remove()
                }
            }
            val functionSuffix = testAnnotation.invocation ?: ""
            when (val parent = ref.parent) {
                is KotlinPackageReference -> codeBlocks.add(
                    CodeBlock.of("print(%S, %N%L)", declaration.name.asString(), ref.swiftReference(), functionSuffix)
                )
                is KotlinTypeReference -> codeBlocks.add(
                    CodeBlock.of("print(%S, %T().%N%L)", declaration.name.asString(), parent.swiftReference(), ref.swiftReference(), functionSuffix)
                )
            }
        }

        return super.visitFunction(declaration)
    }
}

@AutoService(SwiftPackGenerationExtension::class)
class ExampleSwiftPackGenerationExtension: SwiftPackGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("swiftkt-example") {
            val codeBlocks = mutableListOf<CodeBlock>()
            moduleFragment.files.forEach {
                it.accept(TestIrGenerator(this, codeBlocks), null)
            }

            file("TransformTests") {
                addFunction(
                    FunctionSpec.builder("transform")
                        .addCode(
                            codeBlocks.joinToCode("\n")
                        )
                        .addCode("\n")
                        .build()
                )
            }
        }
    }
}
