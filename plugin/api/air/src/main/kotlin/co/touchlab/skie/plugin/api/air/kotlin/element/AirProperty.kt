package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.type.AirType
import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirProperty(
    val symbol: Symbol,
    val name: Name,
    override val origin: AirOrigin,
    override val annotations: List<AirConstantObject>,
    val isExported: Boolean,
    val visibility: AirVisibility,
    val modality: AirModality,
    val overriddenSymbols: List<Symbol>,
    val type: AirType,
    override val containedExpressionSize: Int,
    val isVar: Boolean,
    val isConst: Boolean,
    val isLateinit: Boolean,
    val isDelegated: Boolean,
    val isExternal: Boolean,
    // val isExpect: Boolean is always false at the time we can collect statistics
    val isFakeOverride: Boolean,
    val backingField: AirField?,
    /**
     * IR has the getter optional, however there should always be a getter.
     */
    val getter: AirSimpleFunction,
    val setter: AirSimpleFunction?,
) : AirDeclaration, AirExpressionContainer {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitProperty(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        backingField?.accept(visitor, data)
        getter.accept(visitor, data)
        setter?.accept(visitor, data)
    }

    @Serializable
    data class Name(val name: String)

    @Serializable
    @JvmInline
    value class Symbol(val id: Int)
}
