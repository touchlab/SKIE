package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

interface KotlinClassSwiftModel : KotlinTypeSwiftModel {

    val classDescriptor: ClassDescriptor

    val companionObject: KotlinClassSwiftModel?

    val nestedClasses: List<KotlinClassSwiftModel>

    val enumEntries: List<KotlinEnumEntrySwiftModel>

    val isSealed: Boolean

    val hasUnexposedSealedSubclasses: Boolean

    val exposedSealedSubclasses: List<KotlinClassSwiftModel>
}
