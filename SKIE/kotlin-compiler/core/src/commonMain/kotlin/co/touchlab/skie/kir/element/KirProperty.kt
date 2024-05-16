package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.PropertyConfiguration
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.oir.element.OirCallableDeclaration
import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.sir.element.SirProperty

class KirProperty(
    val kotlinName: String,
    override val kotlinSignature: String,
    val objCName: String,
    val swiftName: String,
    override val owner: KirClass,
    override val origin: KirCallableDeclaration.Origin,
    override val scope: KirScope,
    override val isFakeOverride: Boolean,
    var type: KirType,
    val isVar: Boolean,
    override val deprecationLevel: DeprecationLevel,
    override val isRefinedInSwift: Boolean,
    override val configuration: PropertyConfiguration,
    override val modality: KirCallableDeclaration.Modality,
) : KirOverridableDeclaration<KirProperty, SirProperty> {

    lateinit var oirProperty: OirProperty

    override val oirCallableDeclaration: OirCallableDeclaration
        get() = oirProperty

    private val overridableDeclarationDelegate = KirOverridableDeclarationDelegate(this)

    override val overriddenDeclarations: List<KirProperty> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<KirProperty> by overridableDeclarationDelegate::overriddenBy

    override val originalSirDeclaration: SirProperty
        get() = oirProperty.originalSirProperty

    override val primarySirDeclaration: SirProperty
        get() = oirProperty.primarySirProperty

    override var bridgedSirDeclaration: SirProperty?
        get() = oirProperty.bridgedSirProperty
        set(value) {
            oirProperty.bridgedSirProperty = value
        }

    val originalSirProperty: SirProperty
        get() = oirProperty.originalSirProperty

    val primarySirProperty: SirProperty
        get() = oirProperty.primarySirProperty

    var bridgedSirProperty: SirProperty?
        get() = oirProperty.bridgedSirProperty
        set(value) {
            oirProperty.bridgedSirProperty = value
        }

    init {
        owner.callableDeclarations.add(this)
    }

    override fun addOverride(declaration: KirProperty) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun removeOverride(declaration: KirProperty) {
        overridableDeclarationDelegate.removeOverride(declaration)
    }

    override fun addOverriddenBy(declaration: KirProperty) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun removeOverriddenBy(declaration: KirProperty) {
        overridableDeclarationDelegate.removeOverriddenBy(declaration)
    }

    override fun toString(): String = "${this::class.simpleName}: $kotlinName"
}
