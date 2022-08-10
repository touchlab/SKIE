package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spec.SwiftPackReference

class SwiftReferenceResolver(
    private val references: SwiftPackModule.References,
) {

    fun resolveTypeReference(swiftPackReference: SwiftPackReference): KotlinTypeReference {
        return requireNotNull(references.types[swiftPackReference]) {
            "Could not resolve type reference $swiftPackReference"
        }
    }

    fun resolvePropertyReference(swiftPackReference: SwiftPackReference): KotlinPropertyReference {
        return requireNotNull(references.properties[swiftPackReference]) {
            "Could not resolve property reference $swiftPackReference"
        }
    }

    fun resolveFunctionReference(swiftPackReference: SwiftPackReference): KotlinFunctionReference {
        return requireNotNull(references.functions[swiftPackReference]) {
            "Could not resolve function reference $swiftPackReference"
        }
    }
}
