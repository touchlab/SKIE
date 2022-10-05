package co.touchlab.swiftpack.api.internal

import co.touchlab.swiftpack.api.ReferenceContext
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference

internal interface InternalReferenceContext: ReferenceContext {
    val references: List<KotlinDeclarationReference<*>>
}
