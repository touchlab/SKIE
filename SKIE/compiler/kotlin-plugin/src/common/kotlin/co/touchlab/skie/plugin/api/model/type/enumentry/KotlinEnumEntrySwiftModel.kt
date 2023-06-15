package co.touchlab.skie.plugin.api.model.type.enumentry

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinEnumEntrySwiftModel {

    val descriptor: ClassDescriptor

    val enum: KotlinClassSwiftModel

    val identifier: String
}
