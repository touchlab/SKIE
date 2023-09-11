package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.sir.element.SirClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinTypeSwiftModel {

    val descriptorHolder: ClassOrFileDescriptorHolder

    val visibility: SwiftModelVisibility

    /**
     * All non-removed directly callable members of this type.
     */
    val allAccessibleDirectlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    val allDirectlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    val primarySirClass: SirClass
        get() = bridgedSirClass ?: kotlinSirClass

    val kotlinSirClass: SirClass

    // WIP 2 check Has to be from local module
    val bridgedSirClass: SirClass?

    val objCFqName: ObjcFqName

    val swiftGenericExportScope: SwiftGenericExportScope

    val kind: Kind

    enum class Kind {
        Class, Interface, File;
    }
}

// WIP
fun SirClass.swiftGenericExportScope(classDescriptor: ClassDescriptor): SwiftGenericExportScope {
    return SwiftGenericExportScope.Class(classDescriptor, typeParameters)
}
