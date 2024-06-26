package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.ConstructorConfiguration
import co.touchlab.skie.oir.element.OirConstructor
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.sir.element.SirConstructor

class KirConstructor(
    override val kotlinName: String,
    override val kotlinSignature: String,
    override val objCSelector: String,
    override val swiftName: String,
    override val owner: KirClass,
    override val errorHandlingStrategy: OirFunction.ErrorHandlingStrategy,
    override val deprecationLevel: DeprecationLevel,
    override val configuration: ConstructorConfiguration,
) : KirFunction<SirConstructor>() {

    override val scope: KirScope = KirScope.Static

    override val isRefinedInSwift: Boolean = false

    override val modality: KirCallableDeclaration.Modality = KirCallableDeclaration.Modality.Final

    override val origin: KirCallableDeclaration.Origin = KirCallableDeclaration.Origin.Member

    lateinit var oirConstructor: OirConstructor

    override val oirCallableDeclaration: OirConstructor
        get() = oirConstructor

    override val valueParameters: MutableList<KirValueParameter> = mutableListOf()

    override val defaultArgumentsOverloads: MutableList<KirConstructor> = mutableListOf()

    override val originalSirDeclaration: SirConstructor
        get() = oirConstructor.originalSirConstructor

    override val primarySirDeclaration: SirConstructor
        get() = oirConstructor.primarySirConstructor

    override var bridgedSirDeclaration: SirConstructor?
        get() = oirConstructor.bridgedSirConstructor
        set(value) {
            oirConstructor.bridgedSirConstructor = value
        }

    val originalSirConstructor: SirConstructor
        get() = oirConstructor.originalSirConstructor

    val primarySirConstructor: SirConstructor
        get() = oirConstructor.primarySirConstructor

    var bridgedSirConstructor: SirConstructor?
        get() = oirConstructor.bridgedSirConstructor
        set(value) {
            oirConstructor.bridgedSirConstructor = value
        }

    init {
        owner.callableDeclarations.add(this)
    }
}
