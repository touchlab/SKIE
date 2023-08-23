package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility

interface KotlinDirectlyCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val allBoundedSwiftModels: List<KotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    val visibility: SwiftModelVisibility

    /**
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    /**
     * Examples:
     * foo (for functions and properties)
     * foo(param1:)
     * __foo(param1:) (visibility == Replaced)
     * __Skie_Removed__foo(param1:) (visibility == Removed)
     *
     * Use `reference` to call this function from generated Swift code.
     */
    val reference: String

    /**
     * Examples
     * foo  (for properties)
     * foo() (for functions)
     * foo(param1:)
     * __foo(param1:) (visibility == Replaced)
     * __Skie_Removed__foo(param1:) (visibility == Removed)
     *
     * Use `name` for Api notes and documentation.
     */
    val name: String

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
