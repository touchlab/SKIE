package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class OriginalKotlinClassSwiftModel(
    classDescriptor: ClassDescriptor,
    containingType: KotlinTypeSwiftModel?,
    namer: ObjCExportNamer,
) : BaseKotlinClassSwiftModel(classDescriptor, containingType, namer) {

    override val isChanged: Boolean = false

    override val original: KotlinTypeSwiftModel = this

    override val containingType: KotlinTypeSwiftModel? = containingType?.original

    override val bridge: TypeSwiftModel? = null
}
