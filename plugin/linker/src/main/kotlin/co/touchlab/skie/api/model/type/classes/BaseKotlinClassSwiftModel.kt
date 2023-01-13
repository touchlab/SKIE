package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.util.getClassSwiftName
import co.touchlab.skie.util.stableIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

abstract class BaseKotlinClassSwiftModel(
    classDescriptor: ClassDescriptor,
    namer: ObjCExportNamer,
) : KotlinTypeSwiftModel, KotlinClassSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.Class = ClassOrFileDescriptorHolder.Class(classDescriptor)

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val identifier: String = namer.getClassSwiftName(classDescriptor).split(".").last()

    override val bridge: TypeSwiftModel? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.Class

    override val objCFqName: String = namer.getClassOrProtocolName(classDescriptor).objCName

    override val stableFqName: String =
        KotlinTypeSwiftModel.StableFqNameNamespace +
            ("${classDescriptor.module.stableIdentifier}__${classDescriptor.fqNameSafe.asString()}").replace(".", "_")
}
