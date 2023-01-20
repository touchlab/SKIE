package co.touchlab.skie.plugin.api.model.type.enumentry

import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinEnumEntrySwiftModel {

    val descriptor: ClassDescriptor

    val identifier: String
}
