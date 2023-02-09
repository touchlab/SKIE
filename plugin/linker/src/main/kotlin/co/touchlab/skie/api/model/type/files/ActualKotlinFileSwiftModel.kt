package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.util.swiftIdentifier
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

class ActualKotlinFileSwiftModel(
    private val file: SourceFile,
    module: ModuleDescriptor,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
) : MutableKotlinTypeSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.File = ClassOrFileDescriptorHolder.File(file)

    override var containingType: MutableKotlinClassSwiftModel? = null

    override var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = allDirectlyCallableMembers
            .filterNot { it.visibility.isRemoved }

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = with(swiftModelScope) {
            descriptorProvider.getExposedStaticMembers(file)
                .map { it.swiftModel }
                .flatMap { it.directlyCallableMembers }
        }

    private val fileClassName = namer.getFileClassName(file)

    override var identifier: String = fileClassName.swiftName

    override var bridge: TypeSwiftModel? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.File

    override val objCFqName: String = fileClassName.objCName

    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.None

    override val stableFqName: String = run {
        val moduleFragment = module.swiftIdentifier

        val fileNameFragment =
            file.name?.removeSuffix(".kt")?.let { it + "Kt" }?.toValidSwiftIdentifier() ?: error("File does not have a name.")

        TypeSwiftModel.StableFqNameNamespace + "file__" + moduleFragment + "__" + fileNameFragment
    }

    override val original: KotlinTypeSwiftModel = OriginalKotlinFileSwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge
}
