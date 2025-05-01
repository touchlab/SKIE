package co.touchlab.skie.oir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.sir.element.SirProperty

class OirProperty(
    val name: String,
    val type: OirType,
    val isVar: Boolean,
    override val parent: OirCallableDeclarationParent,
    override val scope: OirScope,
    override val deprecationLevel: DeprecationLevel,
    override val isFakeOverride: Boolean,
) : OirCallableDeclaration,
    OirOverridableDeclaration<OirProperty> {

    lateinit var originalSirProperty: SirProperty

    val primarySirProperty: SirProperty
        get() = bridgedSirProperty ?: originalSirProperty

    var bridgedSirProperty: SirProperty? = null

    override val primarySirCallableDeclaration: SirProperty by ::primarySirProperty

    override val originalSirCallableDeclaration: SirProperty by ::originalSirProperty

    override val bridgedSirCallableDeclaration: SirProperty? by ::bridgedSirProperty

    private val overridableDeclarationDelegate = OirOverridableDeclarationDelegate(this)

    override val overriddenDeclarations: List<OirProperty> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<OirProperty> by overridableDeclarationDelegate::overriddenBy

    init {
        parent.callableDeclarations.add(this)
    }

    override fun addOverride(declaration: OirProperty) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun addOverriddenBy(declaration: OirProperty) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"
}
