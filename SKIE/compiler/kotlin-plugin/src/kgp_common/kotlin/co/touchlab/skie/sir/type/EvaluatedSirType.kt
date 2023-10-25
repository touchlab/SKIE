package co.touchlab.skie.sir.type

import io.outfoxx.swiftpoet.TypeName

data class EvaluatedSirType<out T : SirType>(
    val type: T,
    val isValid: Boolean,
    val canonicalName: String,
    val swiftPoetTypeName: TypeName,
)
