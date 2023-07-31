package co.touchlab.skie.plugin.generator.internal.util.ir

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl

internal fun List<TypeParameterDescriptor>.copy(newOwner: CallableDescriptor): List<TypeParameterDescriptor> =
    this.map { it.copy(newOwner) }

internal fun TypeParameterDescriptor.copy(
    newOwner: CallableDescriptor,
    index: Int = this.index,
): TypeParameterDescriptor =
    TypeParameterDescriptorImpl.createForFurtherModification(
        newOwner,
        this.annotations,
        this.isReified,
        this.variance,
        this.name,
        index,
        this.source,
        this.storageManager,
    ).also { copy ->
        // TODO Check that this works for nested generic types like <T, X : List<T>>
        this.upperBounds.forEach {
            copy.addUpperBound(it)
        }

        copy.setInitialized()
    }
