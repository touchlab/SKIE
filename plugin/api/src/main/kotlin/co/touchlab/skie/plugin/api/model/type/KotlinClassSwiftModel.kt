package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinClassSwiftModel : KotlinTypeSwiftModel {

    val classDescriptor: ClassDescriptor

    val companionObject: KotlinClassSwiftModel?

    val nestedClasses: List<KotlinClassSwiftModel>

    val enumEntries: List<KotlinEnumEntrySwiftModel>

    override val original: KotlinClassSwiftModel
}
