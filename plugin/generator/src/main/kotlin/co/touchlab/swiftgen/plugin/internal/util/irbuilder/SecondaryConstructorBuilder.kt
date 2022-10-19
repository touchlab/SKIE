package co.touchlab.swiftgen.plugin.internal.util.irbuilder

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable

internal class SecondaryConstructorBuilder(val descriptor: ClassConstructorDescriptor) {

    var valueParameters: List<ValueParameterDescriptor> = emptyList()

    var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) once are context implemented properly
    var body: (context(ReferenceSymbolTable) DeclarationIrBuilder.(IrConstructor) -> IrBody)? = null
}
