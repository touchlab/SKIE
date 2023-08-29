package co.touchlab.skie.plugin.generator.internal.util.ir

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.types.KotlinType

internal fun List<ValueParameterDescriptor>.copyWithoutDefaultValue(
    newOwner: CallableDescriptor,
): List<ValueParameterDescriptor> =
    this.mapIndexed { index, valueParameter -> valueParameter.copyWithoutDefaultValue(newOwner, index) }

internal fun ValueParameterDescriptor.copyWithoutDefaultValue(
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
