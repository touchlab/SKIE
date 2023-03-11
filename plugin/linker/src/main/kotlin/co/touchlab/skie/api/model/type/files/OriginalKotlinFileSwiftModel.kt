package co.touchlab.skie.api.model.type.files

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinFileSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration

class OriginalKotlinFileSwiftModel(
    delegate: KotlinFileSwiftModel,
) : KotlinFileSwiftModel by delegate {

    override val visibility: SwiftModelVisibility = delegate.visibility

    override val containingType: KotlinClassSwiftModel? = null

    override val identifier: String = delegate.identifier

    override val bridge: ObjcSwiftBridge? = null

    // override val isChanged: Boolean = false

    override val swiftIrDeclaration: SwiftIrExtensibleDeclaration by lazy {
        TODO()
        // SwiftIrTypeDeclaration.Local.KotlinFile.Immutable(this, "TODO_GIMME_MODULE")
    }
}
