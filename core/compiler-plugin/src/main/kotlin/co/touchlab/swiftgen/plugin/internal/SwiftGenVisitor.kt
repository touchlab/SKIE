package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.plugin.internal.generator.SealedInteropGenerator
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

internal class SwiftGenVisitor(
    output: SwiftPackModuleBuilder,
    moduleFragment: IrModuleFragment,
) : IrElementVisitor<Unit, Unit> {

    private val sealedInteropGenerator = SealedInteropGenerator(output, moduleFragment)

    override fun visitElement(element: IrElement, data: Unit) {
        element.acceptChildren(this, Unit)
    }

    override fun visitClass(declaration: IrClass, data: Unit) {
        super.visitClass(declaration, data)

        sealedInteropGenerator.generate(declaration)
    }
}