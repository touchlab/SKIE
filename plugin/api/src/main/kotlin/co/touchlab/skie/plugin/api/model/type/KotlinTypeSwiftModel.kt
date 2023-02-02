package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.isReplaced

interface KotlinTypeSwiftModel : TypeSwiftModel {

    val descriptorHolder: ClassOrFileDescriptorHolder

    val isChanged: Boolean

    val original: KotlinTypeSwiftModel

    val visibility: SwiftModelVisibility

    /**
     * All non-removed directly callable members of this type.
     */
    val allAccessibleDirectlyCallableMembers: List<KotlinDirectlyCallableMemberSwiftModel>

    override val containingType: KotlinClassSwiftModel?

    /**
     * Examples:
     * Foo
     * Foo (visibility == Replaced)
     */
    override val identifier: String

    val bridge: TypeSwiftModel?

    override val bridgedOrStableFqName: String
        get() = bridge?.stableFqName ?: stableFqName

    val kind: Kind

    val objCFqName: String

    override val isSwiftSymbol: Boolean
        get() = bridge != null

    /**
     * Examples:
     * Foo
     * Bar.Foo
     * __Foo (visibility == Replaced)
     * Bar.__Foo (visibility == Replaced)
     * __Bar.Foo (containingType.visibility == Replaced)
     */
    override fun fqName(separator: String): String {
        val parentName = containingType?.fqName(separator)

        val name = if (visibility.isReplaced) "__$identifier" else identifier

        return if (parentName != null) "$parentName${separator}$name" else name
    }

    enum class Kind {
        Class, File
    }
}
