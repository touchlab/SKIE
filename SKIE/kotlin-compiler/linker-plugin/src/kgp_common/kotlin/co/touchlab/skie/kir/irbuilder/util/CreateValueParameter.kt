package co.touchlab.skie.kir.irbuilder.util

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

fun createValueParameter(owner: FunctionDescriptor, name: Name, index: Int, type: KotlinType): ValueParameterDescriptor =
    ValueParameterDescriptorImpl(
        containingDeclaration = owner,
        original = null,
        index = index,
        annotations = Annotations.EMPTY,
        name = name,
        outType = type,
        declaresDefaultValue = false,
        isCrossinline = false,
        isNoinline = false,
        varargElementType = null,
        source = owner.source,
    )
