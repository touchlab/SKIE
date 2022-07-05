package co.touchlab.swiftpack.spec

import io.outfoxx.swiftpoet.DeclaredTypeName
import kotlinx.serialization.Serializable

@Serializable
sealed interface KobjcTransform {
    @Serializable
    data class HideType(val typeName: String): KobjcTransform
    @Serializable
    data class RenameType(val typeName: String, val newName: String): KobjcTransform
}
