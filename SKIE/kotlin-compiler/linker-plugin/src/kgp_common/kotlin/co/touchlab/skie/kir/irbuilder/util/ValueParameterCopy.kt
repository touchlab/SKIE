package co.touchlab.skie.kir.irbuilder.util

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.types.KotlinType

fun List<ValueParameterDescriptor>.copyWithoutDefaultValue(newOwner: CallableDescriptor): List<ValueParameterDescriptor> =
    this.mapIndexed { index, valueParameter -> valueParameter.copyWithoutDefaultValue(newOwner, index) }

fun ValueParameterDescriptor.copyWithoutDefaultValue(
    newOwner: CallableDescriptor,
    newIndex: Int = this.index,
    newType: KotlinType = this.type,
): ValueParameterDescriptor = ValueParameterDescriptorImpl(
    containingDeclaration = newOwner,
    original = null,
    index = newIndex,
    annotations = Annotations.EMPTY,
    name = this.name,
    outType = newType,
    declaresDefaultValue = false,
    isCrossinline = this.isCrossinline,
    isNoinline = this.isNoinline,
    varargElementType = this.varargElementType,
    source = newOwner.source,
)
