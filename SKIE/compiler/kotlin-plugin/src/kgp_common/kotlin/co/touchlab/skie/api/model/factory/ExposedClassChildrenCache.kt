package co.touchlab.skie.api.model.factory

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers

class ExposedClassChildrenCache(descriptorProvider: DescriptorProvider) {

    private val childrenByClass = descriptorProvider.exposedClasses
        .map { it.original }
        .associateWith { mutableSetOf<ClassDescriptor>() }

    init {
        childrenByClass.keys.forEach { classDescriptor ->
            // No need to filter only exposed classes because they are not present in the map
            classDescriptor.getAllSuperClassifiers()
                .forEach { parent ->
                    childrenByClass[parent]?.add(classDescriptor)
                }
        }
    }

    fun getExposedChildren(classDescriptor: ClassDescriptor): Set<ClassDescriptor> =
        childrenByClass[classDescriptor] ?: emptySet()
}
