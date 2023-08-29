package co.touchlab.skie.api.model.type

import co.touchlab.skie.api.model.callable.function.FakeObjcConstructorKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.type.translation.SwiftIrDeclarationRegistry
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.getAllExposedMembers
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcFqName
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.util.mutableLazy
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.descriptors.isSealed
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny

class ActualKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    private val namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
    private val swiftIrDeclarationRegistry: SwiftIrDeclarationRegistry,
) : MutableKotlinClassSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.Class = ClassOrFileDescriptorHolder.Class(classDescriptor)

    override var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

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
                ?.filter { it.receiver.declaration == this@ActualKotlinClassSwiftModel.swiftIrDeclaration }
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

    private val swiftName = classDescriptor.swiftName

    override var identifier: String = swiftName.name
    override val originalIdentifier: String
    // Needs to be recalculated because the `identifier` is changed if the class is nested inside a non-exported class.
            = namer.getClassOrProtocolName(classDescriptor.original).swiftName.substringAfter(".")

    override var bridge: ObjcSwiftBridge? = null

    override val kind: KotlinTypeSwiftModel.Kind =
        if (classDescriptor.kind.isInterface) KotlinTypeSwiftModel.Kind.Interface else KotlinTypeSwiftModel.Kind.Class

    override val objCFqName: ObjcFqName = ObjcFqName(namer.getClassOrProtocolName(classDescriptor.original).objCName)

    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.Class(classDescriptor, namer)

    private val originalContainingType: MutableKotlinClassSwiftModel? by lazy {
        with(swiftModelScope) {
            when (swiftName) {
                is SwiftFqName.Local.Nested -> classDescriptor.getContainingClassNamed(swiftName.parent.asString()).swiftModel
                is SwiftFqName.Local.TopLevel -> null
            }
        }
    }

    override var containingType: MutableKotlinClassSwiftModel? by mutableLazy {
        originalContainingType
    }

    override val nonBridgedDeclaration: SwiftIrExtensibleDeclaration.Local by lazy {
        with(swiftModelScope) {
            swiftIrDeclarationRegistry.declarationForSwiftModel(this@ActualKotlinClassSwiftModel)
        }
    }

    private val ClassDescriptor.swiftName: SwiftFqName.Local
        get() {
            val fullName = namer.getClassOrProtocolName(this.original).swiftName

            return if (fullName.contains(".")) this.getNestedClassSwiftName(fullName) else getTopLevelClassSwiftName(fullName)
        }

    private fun ClassDescriptor.getTopLevelClassSwiftName(fullName: String): SwiftFqName.Local.TopLevel {
        return SwiftFqName.Local.TopLevel(
            name = fullName,
        )
    }

    private fun ClassDescriptor.getNestedClassSwiftName(fullName: String): SwiftFqName.Local {
        val containingClassName = fullName.substringBefore(".")
        val identifier = fullName.substringAfter(".")

        val containingClass = this.getContainingClassNamed(containingClassName)

        return if (containingClass in descriptorProvider.exposedClasses) {
            SwiftFqName.Local.Nested(
                parent = containingClass.swiftName,
                name = identifier,
            )
        } else {
            val concatenatedIdentifier = containingClassName + identifier.replaceFirstChar(Char::uppercaseChar)

            SwiftFqName.Local.TopLevel(
                name = concatenatedIdentifier,
            )
        }
    }

    private fun ClassDescriptor.getContainingClassNamed(name: String): ClassDescriptor {
        val containingClass = this.containingDeclaration as ClassDescriptor

        val containingClassName = namer.getClassOrProtocolName(containingClass.original).swiftName

        return if (containingClassName == name) containingClass else containingClass.getContainingClassNamed(name)
    }

    override fun toString(): String = classDescriptor.toString()
}
