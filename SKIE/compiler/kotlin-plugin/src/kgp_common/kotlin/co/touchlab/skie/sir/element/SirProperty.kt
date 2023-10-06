package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.Modifier

class SirProperty(
    override var identifier: String,
    parent: SirDeclarationParent,
    var type: SirType,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var scope: SirScope = SirScope.Member,
    attributes: List<String> = emptyList(),
    modifiers: List<Modifier> = emptyList(),
) : SirCallableDeclaration, SirOverridableDeclaration<SirProperty> {

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
            scope: SirScope = SirScope.Member,
            attributes: List<String> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
        ): SirProperty =
            SirProperty(
                identifier = identifier,
                parent = this@SirDeclarationParent,
                type = type,
                visibility = visibility,
                scope = scope,
                attributes = attributes,
                modifiers = modifiers,
            )
    }
}
