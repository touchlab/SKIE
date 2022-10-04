package co.touchlab.swiftpack.api.internal

import co.touchlab.swiftpack.api.ReferenceContext
import co.touchlab.swiftpack.spec.symbol.KotlinSymbol

internal interface InternalReferenceContext: ReferenceContext {
    val references: Map<KotlinSymbol.Id, KotlinSymbol<*>>

    val symbols: List<KotlinSymbol<*>>
}
