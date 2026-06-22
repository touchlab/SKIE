package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.allModules
import co.touchlab.skie.phases.mainSkieContext
import co.touchlab.skie.phases.moduleFragment
import co.touchlab.skie.shim.IrVisitorVoid
import co.touchlab.skie.shim.visitChildren
import co.touchlab.skie.util.extractSwiftDocBefore
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import java.io.File

object CollectKDocFromSourcesPhase : KotlinIrPhase {

    context(KotlinIrPhase.Context)
    override suspend fun execute() {
        val kDocMap = mainSkieContext.kDocMap

        val allFiles = (listOf(moduleFragment) + allModules.values)
            .flatMap { it.files }
            .distinctBy { it.fileEntry.name }

        allFiles.forEach { file ->
            collectKDocsFromFile(file, kDocMap)
        }
    }

    private fun collectKDocsFromFile(file: IrFile, kDocMap: MutableMap<String, String>) {
        val sourceFile = File(file.fileEntry.name)
        if (!sourceFile.exists()) return

        val source = sourceFile.readText()

        val visitor = object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                visitChildren(element)
            }

            override fun visitClass(declaration: IrClass) {
                extractSwiftDocBefore(source, declaration.startOffset)?.let { swiftDoc ->
                    kDocMap[declaration.descriptor.fqNameSafe.asString()] = swiftDoc
                }
                visitChildren(declaration)
            }

            override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                extractSwiftDocBefore(source, declaration.startOffset)?.let { swiftDoc ->
                    kDocMap[declaration.descriptor.fqNameSafe.asString()] = swiftDoc
                }
            }

            override fun visitProperty(declaration: IrProperty) {
                extractSwiftDocBefore(source, declaration.startOffset)?.let { swiftDoc ->
                    kDocMap[declaration.descriptor.fqNameSafe.asString()] = swiftDoc
                }
            }

            override fun visitConstructor(declaration: IrConstructor) {
                extractSwiftDocBefore(source, declaration.startOffset)?.let { swiftDoc ->
                    kDocMap[declaration.descriptor.fqNameSafe.asString()] = swiftDoc
                }
            }

            override fun visitEnumEntry(declaration: IrEnumEntry) {
                extractSwiftDocBefore(source, declaration.startOffset)?.let { swiftDoc ->
                    kDocMap[declaration.descriptor.fqNameSafe.asString()] = swiftDoc
                }
            }
        }
        visitor.visitChildren(file)
    }
}
