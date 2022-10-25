package co.touchlab.skie.plugin.generator.internal.util.irbuilder

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.types.KotlinType

internal class FunctionBuilder(val descriptor: SimpleFunctionDescriptor) {

    var extensionReceiverParameter: ReceiverParameterDescriptor? = null

    var dispatchReceiverParameter: ReceiverParameterDescriptor? = null

    var contextReceiverParameters: List<ReceiverParameterDescriptor> = emptyList()

    var typeParameters: List<TypeParameterDescriptor> = emptyList()

    var valueParameters: List<ValueParameterDescriptor> = emptyList()

    var returnType: KotlinType = descriptor.builtIns.unitType

    var modality: Modality = Modality.FINAL

    var visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC

    var isInline: Boolean = false

    var isSuspend: Boolean = false

    // TODO Change to context(ReferenceSymbolTable, DeclarationIrBuilder) once are context implemented properly
    var body: (context(ReferenceSymbolTable) DeclarationIrBuilder.(IrSimpleFunction) -> IrBody)? = null
}
