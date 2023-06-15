package co.touchlab.skie.plugin.generator.internal.util.irbuilder

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody

internal class SecondaryConstructorBuilder(val descriptor: ClassConstructorDescriptor) {

    var valueParameters: List<ValueParameterDescriptor> = emptyList()

    var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC

    // TODO Change to context(IrPluginContext, DeclarationIrBuilder) once are context implemented properly
    var body: (context(IrPluginContext) DeclarationIrBuilder.(IrConstructor) -> IrBody)? = null
}
