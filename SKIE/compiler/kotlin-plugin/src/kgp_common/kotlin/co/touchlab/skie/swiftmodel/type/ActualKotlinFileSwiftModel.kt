package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftGenericExportScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.isRemoved
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
