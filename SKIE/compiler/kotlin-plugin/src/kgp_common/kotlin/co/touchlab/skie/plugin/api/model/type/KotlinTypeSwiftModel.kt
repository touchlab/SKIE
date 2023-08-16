package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration

interface KotlinTypeSwiftModel {

    val containingType: KotlinTypeSwiftModel?

    val descriptorHolder: ClassOrFileDescriptorHolder

    val visibility: SwiftModelVisibility

    /**
     * All non-removed directly callable members of this type.
     */
    val allAccessibleDirectlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    val allDirectlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    /**
     * Examples:
     * Foo
     * Foo (visibility == Replaced)
     */
    val identifier: String

    val originalIdentifier: String

    val swiftIrDeclaration: SwiftIrExtensibleDeclaration
        get() = bridge?.declaration ?: nonBridgedDeclaration

    val bridge: ObjcSwiftBridge?

    val nonBridgedDeclaration: SwiftIrExtensibleDeclaration.Local

    val kind: Kind

    val objCFqName: ObjcFqName

    val isSwiftSymbol: Boolean
        get() = bridge != null

    val swiftGenericExportScope: SwiftGenericExportScope

    enum class Kind {
        Class, Interface, File;

        val isClass: Boolean
            get() = this == Class

        val isInterface: Boolean
            get() = this == Interface

        val isFile: Boolean
            get() = this == File
    }

    companion object {
        val StableFqNameNamespace = SwiftFqName.Local.TopLevel("__Skie")
    }
}

/**
 * Swift type identifier that includes the containing type.
 * Examples:
 * A
 * A (visibility == Replaced)
 * A.B (B.identifier == B)
 */
val KotlinTypeSwiftModel.fqIdentifier: String
    get() {
        val parentIdentifier = containingType?.fqIdentifier

        return if (parentIdentifier != null) "$parentIdentifier.$identifier" else identifier
    }
