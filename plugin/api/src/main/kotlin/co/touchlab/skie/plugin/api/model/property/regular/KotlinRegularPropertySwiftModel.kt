package co.touchlab.skie.plugin.api.model.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel

interface KotlinRegularPropertySwiftModel : KotlinPropertySwiftModel {

    val isChanged: Boolean

    val original: KotlinRegularPropertySwiftModel

    val visibility: SwiftModelVisibility

    val receiver: KotlinTypeSwiftModel

    /**
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    val objCName: String
}

/**
 * Examples:
 * foo
 * __foo (visibility == Replaced)
 *
 * Use `reference` to call this function from generated Swift code.
 */
val KotlinRegularPropertySwiftModel.reference: String
    get() = if (visibility.isReplaced) "__$identifier" else identifier

/**
 * Properties have their name equal to reference.
 *
 * Use `name` for Api notes and documentation.
 */
val KotlinRegularPropertySwiftModel.name: String
    get() = reference

