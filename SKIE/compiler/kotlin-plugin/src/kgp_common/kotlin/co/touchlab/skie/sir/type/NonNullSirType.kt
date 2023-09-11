package co.touchlab.skie.sir.type

sealed class NonNullSirType : SirType() {

    override fun toNonNull(): NonNullSirType = this
}
