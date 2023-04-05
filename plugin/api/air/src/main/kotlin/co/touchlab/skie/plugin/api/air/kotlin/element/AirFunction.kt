package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.type.AirType
import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirFunction : AirDeclaration, AirExpressionContainer {

    val symbol: Symbol

    val dispatchReceiverParameter: AirValueParameter?

    val extensionReceiverParameter: AirValueParameter?

    val valueParameters: List<AirValueParameter>

    val typeParameters: List<AirTypeParameter>

    val returnType: AirType

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        dispatchReceiverParameter?.accept(visitor, data)
        extensionReceiverParameter?.accept(visitor, data)
        valueParameters.forEach { it.accept(visitor, data) }
        typeParameters.forEach { it.accept(visitor, data) }
    }

    @Serializable
    @JvmInline
    value class Symbol(val id: Int)
}
