package co.touchlab.skie.plugin.api.model.type

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

sealed interface ClassOrFileDescriptorHolder {

    class Class(val value: ClassDescriptor) : ClassOrFileDescriptorHolder

    class File(val value: SourceFile) : ClassOrFileDescriptorHolder
}
