package co.touchlab.skie.plugin.api.sir.element

import co.touchlab.skie.plugin.api.sir.SirFqName
import co.touchlab.skie.plugin.api.sir.element.util.sirDeclarationParent
import io.outfoxx.swiftpoet.ExtensionSpec

class SirExtension(
    var typeDeclaration: SirTypeDeclaration,
    parent: SirTopLevelDeclarationParent,
    var visibility: SirVisibility = SirVisibility.Public,
) : SirDeclaration, SirDeclarationNamespace {

    override var parent: SirTopLevelDeclarationParent by sirDeclarationParent(parent)

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override val fqName: SirFqName
        get() = typeDeclaration.fqName

    val internalName: SirFqName
        get() = typeDeclaration.internalName

    // TODO Replace SwiftPoet with Sir
    val swiftPoetBuilderModifications = mutableListOf<ExtensionSpec.Builder.() -> Unit>()

    override fun toString(): String =
        "${this::class.simpleName}: $fqName"
}
