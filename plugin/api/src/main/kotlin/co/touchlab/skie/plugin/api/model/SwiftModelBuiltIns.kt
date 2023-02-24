package co.touchlab.skie.plugin.api.model

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel

interface SwiftModelBuiltIns {

    val skieFlow: KotlinClassSwiftModel

    val skieOptionalFlow: KotlinClassSwiftModel
}
