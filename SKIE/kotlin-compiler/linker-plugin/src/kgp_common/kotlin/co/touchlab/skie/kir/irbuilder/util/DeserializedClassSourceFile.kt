package co.touchlab.skie.kir.irbuilder.util

import co.touchlab.skie.shim.findPackage
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.konan.kotlinLibrary
import org.jetbrains.kotlin.library.metadata.DeserializedSourceFile
import org.jetbrains.kotlin.library.metadata.KlibMetadataDeserializedPackageFragment
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

fun DeserializedClassDescriptor.findSourceFile(): SourceFile =
    sourceByIndex(this, classProto.getExtension(KlibMetadataProtoBuf.classFile))

private fun sourceByIndex(descriptor: DeclarationDescriptor, index: Int): SourceFile {
    val fragment = descriptor.findPackage() as KlibMetadataDeserializedPackageFragment
    val fileName = fragment.proto.strings.stringList[index]
    return DeserializedSourceFile(fileName, descriptor.module.kotlinLibrary)
}
