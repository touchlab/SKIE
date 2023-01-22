package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.util.mutableLazy
import co.touchlab.skie.util.swiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class ActualKotlinClassSwiftModel(
    override val classDescriptor: ClassDescriptor,
    private val namer: ObjCExportNamer,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinClassSwiftModel {

    override val descriptorHolder: ClassOrFileDescriptorHolder.Class = ClassOrFileDescriptorHolder.Class(classDescriptor)

    override var visibility: SwiftModelVisibility = SwiftModelVisibility.Visible

    override var identifier: String = namer.getClassOrProtocolName(classDescriptor.original).swiftName.split(".").last()

    override var bridge: TypeSwiftModel? = null

    override val kind: KotlinTypeSwiftModel.Kind = KotlinTypeSwiftModel.Kind.Class

    override val objCFqName: String = namer.getClassOrProtocolName(classDescriptor.original).objCName

    override val swiftGenericExportScope: SwiftGenericExportScope = SwiftGenericExportScope.Class(classDescriptor, namer)

    private val originalContainingType by lazy {
        with(swiftModelScope) {
            classDescriptor.containingClass?.swiftModel
        }
    }

    override var containingType: MutableKotlinClassSwiftModel? by mutableLazy {
        originalContainingType
    }

    override val stableFqName: String =
        TypeSwiftModel.StableFqNameNamespace +
            ("class__${classDescriptor.module.swiftIdentifier}__${classDescriptor.fqNameSafe.asString()}").replace(".", "_")

    override val isChanged: Boolean
        get() = identifier != original.identifier ||
            containingType?.isChanged == true ||
            containingType != original.containingType ||
            visibility != original.visibility ||
            bridge != original.bridge

    override val original: KotlinClassSwiftModel = OriginalKotlinClassSwiftModel(this, lazy { originalContainingType })

    private val ClassDescriptor.containingClass: ClassDescriptor?
        get() {
            val fullName = namer.getClassOrProtocolName(this.original).swiftName

            return if (fullName.contains(".")) {
                val containingClassName = fullName.substringBefore(".")

                this.getContainingClassNamed(containingClassName)
            } else {
                null
            }
        }

    private fun ClassDescriptor.getContainingClassNamed(name: String): ClassDescriptor {
        val containingClass = this.containingDeclaration as ClassDescriptor

        val containingClassName = namer.getClassOrProtocolName(containingClass.original).swiftName

        return if (containingClassName == name) containingClass else containingClass.getContainingClassNamed(name)
    }
}
