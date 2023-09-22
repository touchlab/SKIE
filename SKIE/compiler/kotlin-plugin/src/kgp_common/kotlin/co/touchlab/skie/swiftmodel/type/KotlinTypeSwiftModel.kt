package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.swiftmodel.SwiftGenericExportScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel

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

    val bridgedSirClass: SirClass?

    val objCFqName: ObjcFqName

    val swiftGenericExportScope: SwiftGenericExportScope

    val kind: Kind

    enum class Kind {
        Class, Interface, File;
    }
}

