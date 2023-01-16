package co.touchlab.skie.plugin.api.model.type

data class SwiftTypeSwiftModel(
    override val containingType: TypeSwiftModel?,
    override val identifier: String,
    val isHashable: Boolean,
) : TypeSwiftModel {

    override val stableFqName: String
        get() {
            val parentName = containingType?.stableFqName ?: return identifier

            return "$parentName.$identifier"
        }

    override val bridgedOrStableFqName: String
        get() = stableFqName

    override val isSwiftSymbol: Boolean = true

    override fun fqName(separator: String): String {
        val parentName = containingType?.fqName(separator) ?: return identifier

        return "$parentName${separator}$identifier"
    }
}
