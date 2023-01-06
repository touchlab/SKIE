package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class ActualKotlinClassSwiftModel(
    classDescriptor: ClassDescriptor,
    override var containingType: MutableKotlinTypeSwiftModel?,
    namer: ObjCExportNamer,
) : BaseKotlinClassSwiftModel(classDescriptor, containingType, namer), MutableKotlinTypeSwiftModel {

    override var identifier: String = super.identifier

    override var visibility: SwiftModelVisibility = super.visibility

    override var bridge: TypeSwiftModel? = null

    override val original: KotlinTypeSwiftModel = OriginalKotlinClassSwiftModel(classDescriptor, containingType, namer)

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge
}
