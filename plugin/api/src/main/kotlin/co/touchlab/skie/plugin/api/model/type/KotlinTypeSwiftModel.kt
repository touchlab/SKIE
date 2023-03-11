package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration

sealed interface ObjcSwiftBridge {
    val declaration: SwiftIrTypeDeclaration

    data class FromSDK(
        override val declaration: SwiftIrTypeDeclaration,
    ): ObjcSwiftBridge

    data class FromSKIE(
        override val declaration: SwiftIrTypeDeclaration,
    ): ObjcSwiftBridge
}

interface KotlinTypeSwiftModel {

    val containingType: KotlinTypeSwiftModel?

    val descriptorHolder: ClassOrFileDescriptorHolder

    // val isChanged: Boolean

    // val original: KotlinTypeSwiftModel

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

    val nonBridgedDeclaration: SwiftIrExtensibleDeclaration

    // override val stableFqName: SwiftFqName.NominalType

    // override val bridgedOrStableFqName: SwiftFqName
    //     get() = bridge?.stableFqName ?: stableFqName

    val kind: Kind

    val objCFqName: ObjcFqName

    val isSwiftSymbol: Boolean
        get() = bridge != null

    val swiftGenericExportScope: SwiftGenericExportScope

    /**
     * Examples:
     * - Foo
     * - Bar.Foo
     * - __Foo (visibility == Replaced)
     * - Bar.__Foo (visibility == Replaced)
     * - __Bar.Foo (containingType.visibility == Replaced)
     */
    // override fun fqName(separator: String): SwiftFqName.NominalType {
    //     TODO()
    //     // val parentName = containingType?.fqName(separator)
    //     //
    //     // // TODO: Rename `Any` to `{ModuleName}Any`
    //     // val name = if (visibility.isReplaced) identifier.prefixed "__$identifier" else if (identifier == "Any") "`$identifier`" else identifier
    //     //
    //     // return SwiftFqName.Local(if (parentName != null) "$parentName${separator}$name" else name)
    // }

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

        const val DEFAULT_SEPARATOR = "."

        // const val StableFqNameNamespace: String = "__Skie."

        val StableFqNameNamespace = SwiftFqName.Local.TopLevel("__Skie")
    }
}
