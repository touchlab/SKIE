package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.oir.element.OirCallableDeclaration
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.sir.element.SirSimpleFunction

class KirSimpleFunction(
    override val kotlinName: String,
    override val kotlinSignature: String,
    override val objCSelector: String,
    override val swiftName: String,
    override val owner: KirClass,
    override val origin: KirCallableDeclaration.Origin,
    override val isFakeOverride: Boolean,
    val isSuspend: Boolean,
    var returnType: KirType,
    val kind: Kind,
    override val scope: KirScope,
    override val errorHandlingStrategy: OirFunction.ErrorHandlingStrategy,
    override val deprecationLevel: DeprecationLevel,
    override val isRefinedInSwift: Boolean,
    override val configuration: SimpleFunctionConfiguration,
) : KirFunction<SirSimpleFunction>(), KirOverridableDeclaration<KirSimpleFunction, SirSimpleFunction> {

    lateinit var oirSimpleFunction: OirSimpleFunction

    override val oirCallableDeclaration: OirCallableDeclaration
        get() = oirSimpleFunction

    private val overridableDeclarationDelegate = KirOverridableDeclarationDelegate(this)

    override val overriddenDeclarations: List<KirSimpleFunction> by overridableDeclarationDelegate::overriddenDeclarations

    override val overriddenBy: List<KirSimpleFunction> by overridableDeclarationDelegate::overriddenBy

    override val valueParameters: MutableList<KirValueParameter> = mutableListOf()

    override val defaultArgumentsOverloads: MutableList<KirSimpleFunction> = mutableListOf()

    override val originalSirDeclaration: SirSimpleFunction
        get() = oirSimpleFunction.originalSirFunction

    override val primarySirDeclaration: SirSimpleFunction
        get() = oirSimpleFunction.primarySirFunction

    override var bridgedSirDeclaration: SirSimpleFunction?
        get() = oirSimpleFunction.bridgedSirFunction
        set(value) {
            oirSimpleFunction.bridgedSirFunction = value
        }

    val originalSirFunction: SirSimpleFunction
        get() = oirSimpleFunction.originalSirFunction

    val primarySirFunction: SirSimpleFunction
        get() = oirSimpleFunction.primarySirFunction

    var bridgedSirFunction: SirSimpleFunction?
        get() = oirSimpleFunction.bridgedSirFunction
        set(value) {
            oirSimpleFunction.bridgedSirFunction = value
        }

    init {
        owner.callableDeclarations.add(this)
    }

    override fun addOverride(declaration: KirSimpleFunction) {
        overridableDeclarationDelegate.addOverride(declaration)
    }

    override fun removeOverride(declaration: KirSimpleFunction) {
        overridableDeclarationDelegate.removeOverride(declaration)
    }

    override fun addOverriddenBy(declaration: KirSimpleFunction) {
        overridableDeclarationDelegate.addOverriddenBy(declaration)
    }

    override fun removeOverriddenBy(declaration: KirSimpleFunction) {
        overridableDeclarationDelegate.removeOverriddenBy(declaration)
    }

    sealed interface Kind {

        object Function : Kind

        data class PropertyGetter(var associatedSetter: KirSimpleFunction?) : Kind

        data class PropertySetter(var associatedGetter: KirSimpleFunction?) : Kind
    }
}
