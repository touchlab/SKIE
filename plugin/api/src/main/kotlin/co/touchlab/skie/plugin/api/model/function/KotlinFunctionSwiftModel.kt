package co.touchlab.skie.plugin.api.model.function

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

interface KotlinFunctionSwiftModel {

    val descriptor: FunctionDescriptor

    val isChanged: Boolean

    val original: KotlinFunctionSwiftModel

    val visibility: SwiftModelVisibility

    val receiver: KotlinTypeSwiftModel

    /**
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    val parameters: List<Parameter>

    val objCSelector: String

    interface Parameter {

        val original: Parameter

        val isChanged: Boolean

        val argumentLabel: String
    }
}

/**
 * Examples:
 * foo
 * foo(param1:)
 * __foo(param1:) (visibility == Replaced)
 */
val KotlinFunctionSwiftModel.reference: String
    get() = if (parameters.isEmpty()) {
        identifierAfterReplace
    } else {
        "$identifierAfterReplace(${parameters.joinToString("") { "${it.argumentLabel}:" }})"
    }

/**
 * Examples:
 * foo()
 * foo(param1:)
 * __foo(param1:) (visibility == Replaced)
 */
val KotlinFunctionSwiftModel.name: String
    get() = if (parameters.isEmpty()) "$identifierAfterReplace()" else reference

/**
 * Examples:
 * foo
 * __foo (visibility == Replaced)
 */
private val KotlinFunctionSwiftModel.identifierAfterReplace: String
    get() = if (visibility.isReplaced) "__$identifier" else identifier
