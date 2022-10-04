package co.touchlab.swiftpack.spec.module

import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFile
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import co.touchlab.swiftpack.spec.symbol.KotlinType
import kotlinx.serialization.Serializable

@Serializable
sealed interface ApiTransform {
    @Serializable
    data class FileTransform(
        val fileId: KotlinFile.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: TypeTransform.Rename.Action? = null,
        val bridge: String? = null,
    ): ApiTransform

    @Serializable
    data class TypeTransform(
        val typeId: KotlinType.Id,
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
            data class Relative(val parentKotlinClass: KotlinClass.Id, val swiftType: String): Bridge
        }
    }

    @Serializable
    class PropertyTransform(
        val propertyId: KotlinProperty.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class FunctionTransform(
        val functionId: KotlinFunction.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class EnumEntryTransform(
        val enumEntryId: KotlinEnumEntry.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform
}
