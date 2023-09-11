package co.touchlab.skie.plugin.api.sir.type

sealed class NonNullSirType : SirType() {

    override fun toNonNull(): NonNullSirType = this
}
