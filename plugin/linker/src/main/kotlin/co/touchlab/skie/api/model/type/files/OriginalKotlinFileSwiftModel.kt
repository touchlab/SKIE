package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.SourceFile

class OriginalKotlinFileSwiftModel(
    file: SourceFile,
    namer: ObjCExportNamer,
    descriptorProvider: DescriptorProvider,
) : BaseKotlinFileSwiftModel(file, namer, descriptorProvider) {

    override val isChanged: Boolean = false

    override val original: KotlinTypeSwiftModel = this

    override val bridge: TypeSwiftModel? = null
}
