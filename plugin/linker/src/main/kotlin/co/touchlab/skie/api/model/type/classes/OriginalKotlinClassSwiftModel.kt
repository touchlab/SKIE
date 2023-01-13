package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class OriginalKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    containingType: KotlinClassSwiftModel?,
    namer: ObjCExportNamer,
) : BaseKotlinClassSwiftModel(classDescriptor, namer) {

    override val isChanged: Boolean = false

    override val original: KotlinClassSwiftModel = this

    override val containingType: KotlinClassSwiftModel? = containingType?.original

    override val bridge: TypeSwiftModel? = null
}
