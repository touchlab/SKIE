package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.sir.element.SirConstructor

class OirConstructor(
    override val selector: String,
    parent: OirCallableDeclarationParent,
    override val errorHandlingStrategy: ErrorHandlingStrategy,
) : OirFunction(parent) {

    override val scope: OirScope = OirScope.Member

    override val returnType: OirType?
        get() = null

    lateinit var originalSirConstructor: SirConstructor

    val primarySirConstructor: SirConstructor
        get() = bridgedSirConstructor ?: originalSirConstructor

    var bridgedSirConstructor: SirConstructor? = null

    override val primarySirCallableDeclaration: SirConstructor by ::primarySirConstructor

    override val originalSirCallableDeclaration: SirConstructor by ::originalSirConstructor

    override val bridgedSirCallableDeclaration: SirConstructor? by ::bridgedSirConstructor

    override fun toString(): String = "${this::class.simpleName}: $selector"
}
