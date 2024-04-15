package co.touchlab.skie.oir.element

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.sir.element.SirSimpleFunction

class OirSimpleFunction(
    override val selector: String,
    parent: OirCallableDeclarationParent,
    override val scope: OirScope,
    override var returnType: OirType,
    override val errorHandlingStrategy: ErrorHandlingStrategy,
    override val deprecationLevel: DeprecationLevel,
    override val isFakeOverride: Boolean,
) : OirFunction(parent), OirOverridableDeclaration<OirSimpleFunction> {

    lateinit var originalSirFunction: SirSimpleFunction

    val primarySirFunction: SirSimpleFunction
        get() = bridgedSirFunction ?: originalSirFunction

    // TODO Change bridges to only Async bridge and move associated functions concept to Kir
    var bridgedSirFunction: SirSimpleFunction? = null

    override val primarySirCallableDeclaration: SirSimpleFunction by ::primarySirFunction

    override val originalSirCallableDeclaration: SirSimpleFunction by ::originalSirFunction

    override val bridgedSirCallableDeclaration: SirSimpleFunction? by ::bridgedSirFunction

    private val overridableDeclarationDelegate = OirOverridableDeclarationDelegate(this)

    override val overriddenDeclarations: List<OirSimpleFunction> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<OirSimpleFunction> by overridableDeclarationDelegate::overriddenBy

    override fun addOverride(declaration: OirSimpleFunction) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun addOverriddenBy(declaration: OirSimpleFunction) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun toString(): String = "${this::class.simpleName}: $selector"
}
