package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.Modifier
import co.touchlab.skie.kir.element.DeprecationLevel

class SirProperty(
    override var identifier: String,
    parent: SirDeclarationParent,
    var type: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = parent.coerceScope(SirScope.Member),
    override val deprecationLevel: DeprecationLevel = DeprecationLevel.None,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
) : SirOverridableDeclaration<SirProperty>, SirCallableDeclaration {

    override val parent: SirDeclarationParent by sirDeclarationParent(parent)

    override val reference: String
        get() = identifierAfterVisibilityChanges

    override val name: String
        get() = reference

    var getter: SirGetter? = null
        private set

    var setter: SirSetter? = null
        private set

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val modifiers: MutableList<Modifier> = modifiers.toMutableList()

    override val hasValidSignature: Boolean
        get() = type.evaluate().isValid

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

    override fun toString(): String = "${this::class.simpleName}: $name"

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            identifier: String,
            type: SirType,
            visibility: SirVisibility = SirVisibility.Public,
            scope: SirScope = coerceScope(SirScope.Member),
            deprecationLevel: DeprecationLevel = DeprecationLevel.None,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
        ): SirProperty =
            SirProperty(
                identifier = identifier,
                parent = this@SirDeclarationParent,
                type = type,
                visibility = visibility,
                scope = scope,
                deprecationLevel = deprecationLevel,
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
    scope: SirScope = parent.coerceScope(this.scope),
    deprecationLevel: DeprecationLevel = this.deprecationLevel,
    attributes: List<String> = this.attributes,
    modifiers: List<Modifier> = this.modifiers,
): SirProperty =
    SirProperty(
        identifier = identifier,
        parent = parent,
        type = type,
        visibility = visibility,
        scope = scope,
        deprecationLevel = deprecationLevel,
        attributes = attributes,
        modifiers = modifiers,
    )
