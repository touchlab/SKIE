package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.SourceFile

class ActualKotlinFileSwiftModel(
    file: SourceFile,
    namer: ObjCExportNamer,
    descriptorProvider: DescriptorProvider,
) : BaseKotlinFileSwiftModel(file, namer, descriptorProvider), MutableKotlinTypeSwiftModel {

    override var containingType: MutableKotlinClassSwiftModel? = null

    override var identifier: String = super.identifier

    override var visibility: SwiftModelVisibility = super.visibility

    override var bridge: TypeSwiftModel? = super.bridge

    override val original: KotlinTypeSwiftModel = OriginalKotlinFileSwiftModel(file, namer, descriptorProvider)

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge
}
