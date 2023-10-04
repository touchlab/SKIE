package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel

class AsyncKotlinFunctionSwiftModel(
    private val delegate: KotlinFunctionSwiftModelWithCore,
    kotlinSirCallableDeclarationFactory: () -> SirCallableDeclaration,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel by delegate {

    override val kotlinSirCallableDeclaration: SirCallableDeclaration by lazy {
        kotlinSirCallableDeclarationFactory()
    }

    override val kotlinSirFunction: SirFunction
        get() = kotlinSirCallableDeclaration as? SirFunction ?: error("Constructor $kotlinSirCallableDeclaration does not have a SirFunction.")

    override val kotlinSirConstructor: SirConstructor
        get() = kotlinSirCallableDeclaration as? SirConstructor ?: error("Function $kotlinSirCallableDeclaration does not have a SirConstructor.")

    override val primarySirFunction: SirFunction
        get() = bridgedSirFunction ?: kotlinSirFunction

    override var bridgedSirCallableDeclaration: SirCallableDeclaration? = null

    override var bridgedSirConstructor: SirConstructor?
        get() {
            // Check this is constructor
            kotlinSirConstructor

            return bridgedSirCallableDeclaration as? SirConstructor
        }
        set(value) {
            // Check this is constructor
            kotlinSirConstructor

            bridgedSirCallableDeclaration = value
        }

    override var bridgedSirFunction: SirFunction?
        get() {
            // Check this is function
            kotlinSirFunction

            return bridgedSirCallableDeclaration as? SirFunction
        }
        set(value) {
            // Check this is function
            kotlinSirFunction

            bridgedSirCallableDeclaration = value
        }

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = delegate.directlyCallableMembers

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel>
        get() = delegate.valueParameters.filter { it.origin != KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
