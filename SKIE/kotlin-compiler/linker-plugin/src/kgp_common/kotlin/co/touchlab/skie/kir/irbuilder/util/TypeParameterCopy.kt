package co.touchlab.skie.kir.irbuilder.util

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.types.TypeConstructorSubstitution
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.Variance

fun TypeParameterDescriptor.copy(newOwner: CallableDescriptor, index: Int = this.index): TypeParameterDescriptor =
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

fun List<TypeParameterDescriptor>.copyIndexing(newOwner: CallableDescriptor): List<TypeParameterDescriptor> {
    val newDescriptors = mapIndexed { index, oldDescriptor ->
        TypeParameterDescriptorImpl.createForFurtherModification(
            newOwner,
            oldDescriptor.annotations,
            oldDescriptor.isReified,
            oldDescriptor.variance,
            oldDescriptor.name,
            index,
            oldDescriptor.source,
            oldDescriptor.storageManager,
        )
    }

    val typeSubstitutor = TypeSubstitutor.create(
        TypeConstructorSubstitution.createByParametersMap(
            this.zip(newDescriptors).associate { (from, into) ->
                from to TypeProjectionImpl(into.defaultType)
            },
        ),
    )

    forEachIndexed { index, oldDescriptor ->
        val newDescriptor = newDescriptors[index]
        oldDescriptor.upperBounds.forEach { upperBound ->
            val oldTypeParameterIndex = indexOfFirst { it.defaultType == upperBound }
            val upperBoundToAdd = if (oldTypeParameterIndex != -1) {
                newDescriptors[oldTypeParameterIndex].defaultType
            } else {
                upperBound
            }
            newDescriptor.addUpperBound(
                typeSubstitutor.safeSubstitute(upperBoundToAdd, Variance.INVARIANT),
            )
        }
    }

    newDescriptors.forEach {
        it.setInitialized()
    }

    return newDescriptors
}
