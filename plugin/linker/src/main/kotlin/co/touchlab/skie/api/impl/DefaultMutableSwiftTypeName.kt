package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.type.MutableSwiftTypeName

class DefaultMutableSwiftTypeName(
    override val originalParent: MutableSwiftTypeName?,
    override val originalSimpleName: String,
) : MutableSwiftTypeName {

    override var parent: MutableSwiftTypeName? = originalParent
    override var simpleName = originalSimpleName

    override val isChanged: Boolean
        get() = isSimpleNameChanged || parent != originalParent || parent?.isChanged == true

    override val isSimpleNameChanged: Boolean
        get() = simpleName != originalSimpleName

    override val originalQualifiedName: String
        get() = originalQualifiedNameWithSeparators()

    override val qualifiedName: String
        get() = qualifiedNameWithSeparators()
}
