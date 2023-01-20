package co.touchlab.skie.api.model.type.enumentry

import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class ActualKotlinEnumEntrySwiftModel(
    override val descriptor: ClassDescriptor,
    namer: ObjCExportNamer,
) : KotlinEnumEntrySwiftModel {

    override val identifier: String = namer.getEnumEntrySelector(descriptor.original)
}
