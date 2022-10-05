package co.touchlab.swiftlink.plugin.transform

import co.touchlab.swiftpack.spec.module.ApiTransform

class ResolvedName(
    parent: ResolvedName?,
    separator: String,
    val originalSimpleName: String,
    newSimpleName: String? = null,
) {
    var parent = parent
        private set
    var separator = separator
        private set
    var newSwiftName = newSimpleName
        private set

    fun apply(rename: ApiTransform.TypeTransform.Rename) {
        val newSwiftName = when (val action = rename.action) {
            is ApiTransform.TypeTransform.Rename.Action.Prefix -> "${action.prefix}$originalSimpleName"
            is ApiTransform.TypeTransform.Rename.Action.Replace -> action.newName
            is ApiTransform.TypeTransform.Rename.Action.Suffix -> "$originalSimpleName${action.suffix}"
        }

        if (rename.kind == ApiTransform.TypeTransform.Rename.Kind.ABSOLUTE) {
            parent = null
            separator = ""
        }

        this.newSwiftName = newSwiftName
    }

    fun originalQualifiedName(): String {
        val parentName = parent?.originalQualifiedName() ?: return originalSimpleName
        return "$parentName$separator$originalSimpleName"
    }

    fun newQualifiedName(): String {
        val parentName = parent?.newQualifiedName() ?: return newSwiftName ?: originalSimpleName
        return "$parentName$separator${newSwiftName ?: originalSimpleName}"
    }
}
