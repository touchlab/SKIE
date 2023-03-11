package co.touchlab.skie.api.model.type.classes

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrProtocolDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration

class OriginalKotlinClassSwiftModel(
    private val delegate: KotlinClassSwiftModel,
    override val identifier: String,
    swiftIrDeclaration: Lazy<SwiftIrExtensibleDeclaration>,
    containingType: Lazy<KotlinClassSwiftModel?>,
) : KotlinClassSwiftModel by delegate {

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val containingType: KotlinClassSwiftModel? by containingType

    override val swiftIrDeclaration: SwiftIrExtensibleDeclaration by swiftIrDeclaration

    override val bridge: ObjcSwiftBridge? = null

    // override val isChanged: Boolean = false
}
