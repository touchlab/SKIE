package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class ActualKotlinEnumEntrySwiftModel(
    override val descriptor: ClassDescriptor,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
) : KotlinEnumEntrySwiftModel {

    override val identifier: String = namer.getEnumEntrySwiftName(descriptor.original)

    override val enum: KotlinClassSwiftModel
        get() = with(swiftModelScope) {
            (descriptor.containingDeclaration as ClassDescriptor).swiftModel
        }
}
