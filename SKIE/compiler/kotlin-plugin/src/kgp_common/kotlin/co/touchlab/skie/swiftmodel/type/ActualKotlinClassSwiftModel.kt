package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.callable.function.FakeObjcConstructorKotlinFunctionSwiftModel
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.getAllExposedMembers
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftGenericExportScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.isRemoved
import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.sir.element.SirClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.descriptors.isSealed
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny

class ActualKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    override val kotlinSirClass: SirClass,
    namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
) : MutableKotlinClassSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.Class = ClassOrFileDescriptorHolder.Class(classDescriptor)

    override var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible
        set(value) {
            if (field == SwiftModelVisibility.Replaced && kotlinSirClass.simpleName.startsWith("__")) {
                kotlinSirClass.simpleName = kotlinSirClass.simpleName.removePrefix("__")
            }

            if (value == SwiftModelVisibility.Replaced) {
                kotlinSirClass.simpleName = "__${kotlinSirClass.simpleName}"
            }

            field = value
        }

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = allDirectlyCallableMembers.filterNot { it.visibility.isRemoved }

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel> by lazy {
        actualDirectlyCallableMembers + fakeObjcConstructors
    }

    private val actualDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = with(swiftModelScope) {
            descriptorProvider.getAllExposedMembers(classDescriptor)
                .map { it.swiftModel }
                .flatMap { it.directlyCallableMembers }
        }

    private val fakeObjcConstructors: List<MutableKotlinFunctionSwiftModel>
        get() = with(swiftModelScope) {
            classDescriptor.getSuperClassNotAny()
                ?.swiftModelOrNull
                ?.allDirectlyCallableMembers
                ?.flatMap { it.allBoundedSwiftModels }
                ?.filterIsInstance<FakeObjcConstructorKotlinFunctionSwiftModel>()
                ?.filter { it.owner == this@ActualKotlinClassSwiftModel }
                ?: emptyList()
        }

    override val companionObject: MutableKotlinClassSwiftModel? by lazy {
        with(swiftModelScope) {
            descriptorProvider.getExposedCompanionObject(classDescriptor)?.swiftModel
        }
    }

    override val enumEntries: List<KotlinEnumEntrySwiftModel> by lazy {
        with(swiftModelScope) {
            descriptorProvider.getExposedEnumEntries(classDescriptor).map { it.enumEntrySwiftModel }
        }
    }

    override val nestedClasses: List<MutableKotlinClassSwiftModel> by lazy {
        with(swiftModelScope) {
            descriptorProvider.getExposedNestedClasses(classDescriptor).map { it.swiftModel }
        }
    }

    override val isSealed: Boolean = classDescriptor.isSealed()

    override val hasUnexposedSealedSubclasses: Boolean by lazy {
        with(swiftModelScope) {
            classDescriptor.sealedSubclasses.any { it.swiftModelOrNull == null }
        }
    }

    override val exposedSealedSubclasses: List<KotlinClassSwiftModel> by lazy {
        with(swiftModelScope) {
            classDescriptor.sealedSubclasses.mapNotNull { it.swiftModelOrNull }
        }
    }

    override var bridgedSirClass: SirClass? = null

    override val kind: KotlinTypeSwiftModel.Kind =
        if (classDescriptor.kind.isInterface) KotlinTypeSwiftModel.Kind.Interface else KotlinTypeSwiftModel.Kind.Class

    override val objCFqName: ObjcFqName = ObjcFqName(namer.getClassOrProtocolName(classDescriptor.original).objCName)

    override val swiftGenericExportScope: SwiftGenericExportScope by lazy {
        SwiftGenericExportScope.Class(this)
    }

    override fun toString(): String = classDescriptor.toString()
}
