package co.touchlab.swiftgen.plugin.internal.util.ir

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext

internal interface DeclarationTemplate<D : DeclarationDescriptor> {

    val descriptor: D

    fun generateIr(parent: IrDeclarationContainer, generatorContext: GeneratorContext)
}
