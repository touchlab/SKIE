package co.touchlab.swiftpack.spec.module

import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFileReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiTransform {
    @Serializable
    data class FileTransform(
        val fileId: KotlinFileReference.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: TypeTransform.Rename.Action? = null,
        val bridge: String? = null,
    ): ApiTransform

    @Serializable
    data class TypeTransform(
        val typeId: KotlinTypeReference.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: Rename? = null,
        val bridge: Bridge? = null,
    ): ApiTransform {
        @Serializable
        data class Rename(val kind: Kind, val action: Action) {
            @Serializable
            enum class Kind {
                ABSOLUTE, RELATIVE
            }

            @Serializable
            sealed interface Action {
                @Serializable
                class Prefix(val prefix: String) : Action
                @Serializable
                class Suffix(val suffix: String) : Action
                @Serializable
                class Replace(val newName: String) : Action
            }
        }

        @Serializable
        sealed interface Bridge {
            @Serializable
            data class Absolute(val swiftType: String): Bridge
            @Serializable
            data class Relative(val parentKotlinClass: KotlinClassReference.Id, val swiftType: String): Bridge
        }
    }

    @Serializable
    class PropertyTransform(
        val propertyId: KotlinPropertyReference.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class FunctionTransform(
        val functionId: KotlinFunctionReference.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class EnumEntryTransform(
        val enumEntryId: KotlinEnumEntryReference.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform
}
