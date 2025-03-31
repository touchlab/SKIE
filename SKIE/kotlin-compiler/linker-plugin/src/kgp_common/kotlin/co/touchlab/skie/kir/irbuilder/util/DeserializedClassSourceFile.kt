package co.touchlab.skie.kir.irbuilder.util

import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

expect fun DeserializedClassDescriptor.findSourceFile(): SourceFile
