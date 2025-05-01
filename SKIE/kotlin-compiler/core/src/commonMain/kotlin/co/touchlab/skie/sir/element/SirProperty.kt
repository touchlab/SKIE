package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.signature.Signature
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.Modifier

class SirProperty(
    override var identifier: String,
    parent: SirDeclarationParent,
    var type: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var modality: SirModality = parent.coerceModalityForSimpleFunctionOrProperty(),
    override val isAbstract: Boolean = false,
    override var isReplaced: Boolean = false,
    override var isHidden: Boolean = false,
    override var scope: SirScope = parent.coerceScope(SirScope.Member),
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
    override val isFakeOverride: Boolean = false,
    override var isWrappedBySkie: Boolean = false,
    var isMutable: Boolean = false,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
) : SirOverridableDeclaration<SirProperty>,
    SirCallableDeclaration,
    SirElementWithModality {

    override val parent: SirDeclarationParent by sirDeclarationParent(parent)

    override var documentation: String = ""

    override val identifierAfterVisibilityChange: String
        get() = if (isReplaced) "__$identifier" else identifier

    override val reference: String
        get() = identifierAfterVisibilityChange.escapeSwiftIdentifier()

    override val name: String
        get() = identifierAfterVisibilityChange

    var getter: SirGetter? = null
        private set

    var setter: SirSetter? = null
        private set

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    private val overridableDeclarationDelegate = SirOverridableDeclarationDelegate(this)

    override val memberOwner: SirClass? by overridableDeclarationDelegate::memberOwner

    override val overriddenDeclarations: List<SirProperty> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<SirProperty> by overridableDeclarationDelegate::overriddenBy

    override fun addOverride(declaration: SirProperty) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun removeOverride(declaration: SirProperty) {
        overridableDeclarationDelegate.removeOverride(declaration)
    }

    override fun addOverriddenBy(declaration: SirProperty) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun removeOverriddenBy(declaration: SirProperty) {
        overridableDeclarationDelegate.removeOverriddenBy(declaration)
    }

    fun setGetterInternal(getter: SirGetter) {
        this.getter = getter
    }

    fun setSetterInternal(setter: SirSetter) {
        this.setter = setter
    }

    override fun toString(): String = Signature(this).toString()

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            type: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            modality: SirModality = coerceModalityForSimpleFunctionOrProperty(),
            isAbstract: Boolean = false,
            isReplaced: Boolean = false,
            isHidden: Boolean = false,
            scope: SirScope = coerceScope(SirScope.Member),
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
            isFakeOverride: Boolean = false,
            isWrappedBySkie: Boolean = false,
            isMutable: Boolean = false,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
        ): SirProperty = SirProperty(
            identifier = identifier,
            parent = this@SirDeclarationParent,
            type = type,
            visibility = visibility,
            modality = modality,
            isAbstract = isAbstract,
            isReplaced = isReplaced,
            isHidden = isHidden,
            scope = scope,
            deprecationLevel = deprecationLevel,
            isFakeOverride = isFakeOverride,
            isWrappedBySkie = isWrappedBySkie,
            isMutable = isMutable,
            attributes = attributes,
            modifiers = modifiers,
        )
    }
}

fun SirProperty.shallowCopy(
    identifier: String = this.identifier,
    parent: SirDeclarationParent = this.parent,
    type: SirType = this.type,
    visibility: SirVisibility = this.visibility,
    modality: SirModality = parent.coerceModalityForSimpleFunctionOrProperty(this.modality),
    isAbstract: Boolean = this.isAbstract,
    isReplaced: Boolean = this.isReplaced,
    isHidden: Boolean = this.isHidden,
    scope: SirScope = parent.coerceScope(this.scope),
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
    isFakeOverride: Boolean = this.isFakeOverride,
    isWrappedBySkie: Boolean = false,
    isMutable: Boolean = this.isMutable,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
): SirProperty = SirProperty(
    identifier = identifier,
    parent = parent,
    type = type,
    visibility = visibility,
    modality = modality,
    isAbstract = isAbstract,
    isReplaced = isReplaced,
    isHidden = isHidden,
    scope = scope,
    deprecationLevel = deprecationLevel,
    isFakeOverride = isFakeOverride,
    isWrappedBySkie = isWrappedBySkie,
    isMutable = isMutable,
    attributes = attributes,
    modifiers = modifiers,
)

val SirProperty.isOverriddenFromReadOnlyProperty: Boolean
    get() = overriddenDeclarations.any { it.setter == null || it.isOverriddenFromReadOnlyProperty }
