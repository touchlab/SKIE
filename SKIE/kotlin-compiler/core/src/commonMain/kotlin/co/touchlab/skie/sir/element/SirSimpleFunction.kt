package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.signature.Signature
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.Modifier

class SirSimpleFunction(
    override var identifier: String,
    parent: SirDeclarationParent,
    var returnType: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var modality: SirModality = parent.coerceModalityForSimpleFunctionOrProperty(),
    override val isAbstract: Boolean = false,
    override var isReplaced: Boolean = false,
    override var isHidden: Boolean = false,
    override var scope: SirScope = parent.coerceScope(SirScope.Member),
    override val isFakeOverride: Boolean = false,
    override var isWrappedBySkie: Boolean = false,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
    var isAsync: Boolean = false,
    override var throws: Boolean = false,
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
) : SirFunction(attributes.toMutableList(), modifiers.toMutableList()),
    SirOverridableDeclaration<SirSimpleFunction>,
    SirElementWithModality {

    override val identifierAfterVisibilityChange: String
        get() = if (isReplaced) "__$identifier" else identifier

    override val identifierForReference: String
        get() = identifierAfterVisibilityChange.escapeSwiftIdentifier()

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

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

    override fun toString(): String = Signature(this).toString()

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            returnType: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            modality: SirModality = coerceModalityForSimpleFunctionOrProperty(),
            isAbstract: Boolean = false,
            isReplaced: Boolean = false,
            isHidden: Boolean = false,
            scope: SirScope = coerceScope(SirScope.Member),
            isFakeOverride: Boolean = false,
            isWrappedBySkie: Boolean = false,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            isAsync: Boolean = false,
            throws: Boolean = false,
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
        ): SirSimpleFunction = SirSimpleFunction(
            identifier = identifier,
            parent = this@SirDeclarationParent,
            returnType = returnType,
            visibility = visibility,
            modality = modality,
            isAbstract = isAbstract,
            isReplaced = isReplaced,
            isHidden = isHidden,
            scope = scope,
            isFakeOverride = isFakeOverride,
            isWrappedBySkie = isWrappedBySkie,
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
    modality: SirModality = parent.coerceModalityForSimpleFunctionOrProperty(this.modality),
    isAbstract: Boolean = this.isAbstract,
    isReplaced: Boolean = this.isReplaced,
    isHidden: Boolean = this.isHidden,
    scope: SirScope = parent.coerceScope(this.scope),
    isFakeOverride: Boolean = this.isFakeOverride,
    isWrappedBySkie: Boolean = false,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
    isAsync: Boolean = this.isAsync,
    throws: Boolean = this.throws,
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
): SirSimpleFunction = SirSimpleFunction(
    identifier = identifier,
    parent = parent,
    returnType = returnType,
    visibility = visibility,
    modality = modality,
    isAbstract = isAbstract,
    isReplaced = isReplaced,
    isHidden = isHidden,
    scope = scope,
    isFakeOverride = isFakeOverride,
    isWrappedBySkie = isWrappedBySkie,
    attributes = attributes,
    modifiers = modifiers,
    isAsync = isAsync,
    throws = throws,
    deprecationLevel = deprecationLevel,
)

fun SirDeclarationParent.coerceModalityForSimpleFunctionOrProperty(modality: SirModality = SirModality.ModuleLimited): SirModality =
    when (this) {
        is SirClass -> when (this.modality) {
            SirModality.Final -> SirModality.Final
            SirModality.ModuleLimited, SirModality.Open -> modality
        }
        is SirExtension, SirDeclarationParent.None, is SirBuiltInFile, is SirIrFile -> SirModality.Final
    }
