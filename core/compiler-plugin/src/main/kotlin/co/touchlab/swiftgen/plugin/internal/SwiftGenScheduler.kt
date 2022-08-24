package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.generator.SealedInteropGenerator
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

internal class SwiftGenScheduler(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: SwiftGenConfiguration,
    private val reporter: Reporter,
) {

    private val sealedInteropGenerator = SealedInteropGenerator(
        fileBuilderFactory = fileBuilderFactory,
        namespaceProvider = namespaceProvider,
        configuration = configuration.sealedInteropDefaults,
        reporter = reporter,
    )

    fun process(element: IrElement) {
        Visitor().visitElement(element, Unit)
    }

    private inner class Visitor : IrElementVisitor<Unit, Unit> {

        override fun visitElement(element: IrElement, data: Unit) {
            element.acceptChildren(this, Unit)
        }

        override fun visitClass(declaration: IrClass, data: Unit) {
            super.visitClass(declaration, data)

            sealedInteropGenerator.generate(declaration)
        }
    }
}