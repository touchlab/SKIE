package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.phases.memberconflicts.Signature
import co.touchlab.skie.phases.memberconflicts.SirHierarchyCache
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.Modifier

class SirSimpleFunction(
    override var identifier: String,
    parent: SirDeclarationParent,
    var returnType: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = parent.coerceScope(SirScope.Member),
    override val isFakeOverride: Boolean = false,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isAsync: Boolean = false,
    override var throws: Boolean = false,
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
) : SirFunction(attributes.toMutableList(), modifiers.toMutableList()), SirTypeParameterParent, SirOverridableDeclaration<SirSimpleFunction> {

    override val identifierAfterVisibilityChange: String
        get() = when (visibility) {
            SirVisibility.PublicButReplaced -> "__$identifier"
            else -> identifier
        }

    override val identifierForReference: String
        get() = identifierAfterVisibilityChange.escapeSwiftIdentifier()

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val valueParameters: MutableList<SirValueParameter> = mutableListOf()

    private val overridableDeclarationDelegate = SirOverridableDeclarationDelegate(this)

    override val memberOwner: SirClass? by overridableDeclarationDelegate::memberOwner

    override val overriddenDeclarations: List<SirSimpleFunction> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<SirSimpleFunction> by overridableDeclarationDelegate::overriddenBy

    override fun addOverride(declaration: SirSimpleFunction) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun removeOverride(declaration: SirSimpleFunction) {
        overridableDeclarationDelegate.removeOverride(declaration)
    }

    override fun addOverriddenBy(declaration: SirSimpleFunction) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun removeOverriddenBy(declaration: SirSimpleFunction) {
        overridableDeclarationDelegate.removeOverriddenBy(declaration)
    }

    override fun toString(): String =
        Signature(this).toString()

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            returnType: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            scope: SirScope = coerceScope(SirScope.Member),
            isFakeOverride: Boolean = false,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isAsync: Boolean = false,
            throws: Boolean = false,
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
        ): SirSimpleFunction =
            SirSimpleFunction(
                identifier = identifier,
                parent = this@SirDeclarationParent,
                returnType = returnType,
                visibility = visibility,
                scope = scope,
                isFakeOverride = isFakeOverride,
                attributes = attributes,
                modifiers = modifiers,
                isAsync = isAsync,
                throws = throws,
                deprecationLevel = deprecationLevel,
            )
    }
}

fun SirSimpleFunction.shallowCopy(
    identifier: String = this.identifier,
    parent: SirDeclarationParent = this.parent,
    returnType: SirType = this.returnType,
    visibility: SirVisibility = this.visibility,
    scope: SirScope = parent.coerceScope(this.scope),
    isFakeOverride: Boolean = this.isFakeOverride,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
    isAsync: Boolean = this.isAsync,
    throws: Boolean = this.throws,
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
): SirSimpleFunction =
    SirSimpleFunction(
        identifier = identifier,
        parent = parent,
        returnType = returnType,
        visibility = visibility,
        scope = scope,
        isFakeOverride = isFakeOverride,
        attributes = attributes,
        modifiers = modifiers,
        isAsync = isAsync,
        throws = throws,
        deprecationLevel = deprecationLevel,
    )
