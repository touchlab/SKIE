package co.touchlab.skie.plugin.api.model.property

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

interface KotlinPropertySwiftModel {

    val descriptor: PropertyDescriptor

    val isChanged: Boolean

    val original: KotlinPropertySwiftModel

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
 */
val KotlinPropertySwiftModel.name: String
    get() = if (visibility.isReplaced) "__$identifier" else identifier

val KotlinPropertySwiftModel.reference: String
    get() = name
