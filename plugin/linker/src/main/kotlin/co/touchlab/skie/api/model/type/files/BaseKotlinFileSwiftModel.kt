package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.util.sanitizeForIdentifier
import co.touchlab.skie.util.stableIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.SourceFile

abstract class BaseKotlinFileSwiftModel(
    file: SourceFile,
    namer: ObjCExportNamer,
    descriptorProvider: DescriptorProvider,
) : KotlinTypeSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.File = ClassOrFileDescriptorHolder.File(file)

    override val containingType: KotlinTypeSwiftModel? = null

    override val visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    private val fileClassName = namer.getFileClassName(file)

    override val identifier: String = fileClassName.swiftName

    override val bridge: TypeSwiftModel? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.File

    override val objCFqName: String = fileClassName.objCName

    override val stableFqName: String = run {
        val moduleFragment = descriptorProvider.getModuleForFile(file).stableIdentifier

        val fileNameFragment =
            file.name?.removeSuffix(".kt")?.let { it + "Kt" }?.sanitizeForIdentifier() ?: error("File does not have a name.")

        KotlinTypeSwiftModel.StableFqNameNamespace + moduleFragment + "__" + fileNameFragment
    }
}
