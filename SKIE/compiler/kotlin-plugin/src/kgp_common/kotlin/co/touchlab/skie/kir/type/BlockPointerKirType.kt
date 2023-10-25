package co.touchlab.skie.kir.type

import org.jetbrains.kotlin.types.KotlinType

data class BlockPointerKirType(val kotlinType: KotlinType, val returnsVoid: Boolean) : KirType()
