package co.touchlab.skie.plugin.api.model.type

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

sealed interface ClassOrFileDescriptorHolder {

    data class Class(val value: ClassDescriptor) : ClassOrFileDescriptorHolder

    data class File(val value: SourceFile) : ClassOrFileDescriptorHolder
}
