package co.touchlab.skie.swiftmodel.type.enumentry

import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinEnumEntrySwiftModel {

    val descriptor: ClassDescriptor

    val enum: KotlinClassSwiftModel

    val identifier: String
}
