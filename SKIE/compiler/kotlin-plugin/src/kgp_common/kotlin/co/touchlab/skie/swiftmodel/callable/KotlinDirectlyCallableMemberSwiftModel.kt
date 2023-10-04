package co.touchlab.skie.swiftmodel.callable

import co.touchlab.skie.sir.element.SirCallableDeclaration

interface KotlinDirectlyCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    val primarySirCallableDeclaration: SirCallableDeclaration
        get() = bridgedSirCallableDeclaration ?: kotlinSirCallableDeclaration

    val kotlinSirCallableDeclaration: SirCallableDeclaration

    val bridgedSirCallableDeclaration: SirCallableDeclaration?

    override val allBoundedSwiftModels: List<KotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    val collisionResolutionStrategy: CollisionResolutionStrategy

    /**
     * Signature is not valid if and only if it references a SkieErrorType.
     * Only valid signatures can be used in generated Swift code.
     * Invalid signatures can be used only for generating placeholder declaration that cannot be called.
     * Example of such situation is if the signature contains a lambda type argument, such as A<() -> Unit>.
     */
    val hasValidSignatureInSwift: Boolean

    fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT

    sealed interface CollisionResolutionStrategy {

        object Rename : CollisionResolutionStrategy

        /**
         * Members with lower priority are removed later.
         */
        data class Remove(val priority: Int) : CollisionResolutionStrategy
    }
}
