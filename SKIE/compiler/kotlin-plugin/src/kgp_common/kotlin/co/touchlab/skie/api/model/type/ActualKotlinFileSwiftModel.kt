package co.touchlab.skie.api.model.type

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinFileSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcFqName
import co.touchlab.skie.plugin.api.sir.element.SirClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.SourceFile

class ActualKotlinFileSwiftModel(
    private val file: SourceFile,
    override val kotlinSirClass: SirClass,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
) : MutableKotlinFileSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.File = ClassOrFileDescriptorHolder.File(file)

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

    override var bridgedSirClass: SirClass? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.File

    override val objCFqName: ObjcFqName = ObjcFqName(fileClassName.objCName)

    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.None
}
