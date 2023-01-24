package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

interface TypeSwiftModel {

    val containingType: TypeSwiftModel?

    val identifier: String

    /**
     * Points to the original ObjC class.
     * This name does not change except when the type itself changes.
     * For example, it does not change when a new type with the same simple name is added.
     */
    // TODO Rename to objCStableFqName
    val stableFqName: String

    /**
     * `stableFqName` for the final Swift class.
     */
    // TODO Rename to fqName and point to bridged.fqName -> fqName to rawFqName
    val bridgedOrStableFqName: String

    val isSwiftSymbol: Boolean

    val swiftGenericExportScope: SwiftGenericExportScope

    // TODO Remove and keep only fqName also rename to localFqName
    fun fqName(separator: String = DEFAULT_SEPARATOR): String

    // TODO Introduce FqName without replace prefix (copy from NestedBridgeTypesApiNotesFix)

    companion object {

        const val DEFAULT_SEPARATOR = "."

        const val StableFqNameNamespace: String = "__Skie."
    }
}

val TypeSwiftModel.fqName: String
    get() = fqName()

/**
 * A -> A
 * A.B.C -> C
 *
 * (Cannot be based on identifier because fqName implementation might differ)
 */
val TypeSwiftModel.simpleName: String
    get() = fqName.substringAfterLast('.')

/**
 * A -> ε
 * A.B.C -> A.B
 */
val TypeSwiftModel.packageName: String
    get() = fqName.substringBeforeLast('.', "")

val TypeSwiftModel.stableSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName(this.stableFqName)

val TypeSwiftModel.bridgedOrStableSpec: DeclaredTypeName
    get() = DeclaredTypeName.qualifiedLocalTypeName(this.bridgedOrStableFqName)
