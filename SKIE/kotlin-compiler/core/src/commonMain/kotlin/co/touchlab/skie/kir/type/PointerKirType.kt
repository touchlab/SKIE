package co.touchlab.skie.kir.type

data class PointerKirType(val pointee: KirType, val nullable: Boolean) : KirType()
