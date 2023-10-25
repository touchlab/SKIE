package co.touchlab.skie.kir.type

import org.jetbrains.kotlin.types.KotlinType

data class SuspendCompletionKirType(val kotlinType: KotlinType, val useUnitCompletion: Boolean) : KirType()
