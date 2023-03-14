package co.touchlab.skie.api.model.type

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class ActualKotlinEnumEntrySwiftModel(
    override val descriptor: ClassDescriptor,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
) : KotlinEnumEntrySwiftModel {

    override val identifier: String = namer.getEnumEntrySelector(descriptor.original)

    override val enum: KotlinClassSwiftModel
        get() = with(swiftModelScope) {
            (descriptor.containingDeclaration as ClassDescriptor).swiftModel
        }
}
