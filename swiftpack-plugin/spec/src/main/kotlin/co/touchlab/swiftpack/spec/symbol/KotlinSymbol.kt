package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
sealed interface KotlinSymbol<ID: KotlinSymbol.Id> {
    val id: ID
    val signature: IdSignature

    @Serializable
    sealed interface Id
}
