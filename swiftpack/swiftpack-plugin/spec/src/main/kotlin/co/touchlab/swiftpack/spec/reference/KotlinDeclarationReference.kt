package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
sealed interface KotlinDeclarationReference<ID: KotlinDeclarationReference.Id> {
    val id: ID
    val signature: IdSignature

    @Serializable
    sealed interface Id
}
