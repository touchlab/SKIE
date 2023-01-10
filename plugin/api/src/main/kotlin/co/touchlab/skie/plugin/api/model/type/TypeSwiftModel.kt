package co.touchlab.skie.plugin.api.model.type

interface TypeSwiftModel {

    val containingType: TypeSwiftModel?

    val identifier: String

    /**
     * Points to the original ObjC class.
     * This name does not change except when the type itself changes.
     * For example, it does not change when a new type with the same simple name is added.
     */
    val stableFqName: String

    /**
     * `stableFqName` for the final Swift class.
     */
    val bridgedOrStableFqName: String

    fun fqName(separator: String = DEFAULT_SEPARATOR): String

    companion object {

        const val DEFAULT_SEPARATOR = "."
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
