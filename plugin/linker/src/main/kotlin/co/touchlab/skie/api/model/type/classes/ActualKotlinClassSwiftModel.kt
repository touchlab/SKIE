package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class ActualKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    override var containingType: MutableKotlinClassSwiftModel?,
    namer: ObjCExportNamer,
) : BaseKotlinClassSwiftModel(classDescriptor, namer), MutableKotlinClassSwiftModel {

    override var identifier: String = super.identifier

    override var visibility: SwiftModelVisibility = super.visibility

    override var bridge: TypeSwiftModel? = null

    override val original: KotlinClassSwiftModel = OriginalKotlinClassSwiftModel(classDescriptor, containingType, namer)

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge
}
