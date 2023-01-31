package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility

interface KotlinDirectlyCallableMemberSwiftModel : KotlinCallableMemberSwiftModel {

    override val original: KotlinDirectlyCallableMemberSwiftModel

    override val allBoundedSwiftModels: List<KotlinDirectlyCallableMemberSwiftModel>

    override val directlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>
        get() = listOf(this)

    val isChanged: Boolean

    val visibility: SwiftModelVisibility

    /**
     * Examples:
     * foo
     * foo (visibility == Replaced)
     */
    val identifier: String

    /**
     * Examples:
     * foo
     * foo(param1:)
     * __foo(param1:) (visibility == Replaced)
     * __Skie_Removed__foo(param1:) (visibility == Removed)
     *
     * Use `reference` to call this function from generated Swift code.
     */
    val reference: String

    /**
     * Examples:
     * foo()
     * foo(param1:)
     * __foo(param1:) (visibility == Replaced)
     * __Skie_Removed__foo(param1:) (visibility == Removed)
     *
     * Use `name` for Api notes and documentation.
     */
    val name: String

    val collisionResolutionStrategy: CollisionResolutionStrategy

    fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT

    sealed interface CollisionResolutionStrategy {

        object Rename : CollisionResolutionStrategy

        /**
         * Members with lower priority are removed later.
         */
        data class Remove(val priority: Int) : CollisionResolutionStrategy
    }
}
