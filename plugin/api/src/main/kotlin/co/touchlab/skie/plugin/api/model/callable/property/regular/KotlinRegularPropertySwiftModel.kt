package co.touchlab.skie.plugin.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel

interface KotlinRegularPropertySwiftModel : KotlinPropertySwiftModel {

    val isChanged: Boolean

    val original: KotlinRegularPropertySwiftModel

    val visibility: SwiftModelVisibility

    /**
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    val objCName: String

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
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

