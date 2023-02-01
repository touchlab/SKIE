package co.touchlab.skie.plugin.api.model

data class SwiftExportScope(
    val genericScope: SwiftGenericExportScope,
    val flags: Set<Flags>,
) {

    constructor(
        genericScope: SwiftGenericExportScope,
        vararg flags: Flags,
    ) : this(genericScope, flags.toSet())

    fun replacingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, flags.toSet())

    fun addingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, flags.toSet() + this.flags)

    fun removingFlags(vararg flags: Flags): SwiftExportScope = SwiftExportScope(genericScope, this.flags - flags.toSet())

    fun hasFlag(flag: Flags): Boolean = flags.contains(flag)

    fun hasAllFlags(vararg flags: Flags) = flags.all(::hasFlag)

    enum class Flags {
        Escaping,
        ReferenceType,
        Hashable,
    }
}
