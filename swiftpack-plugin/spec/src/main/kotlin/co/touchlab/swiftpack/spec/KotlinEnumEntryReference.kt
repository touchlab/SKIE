package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinEnumEntryReference(val enumType: KotlinTypeReference, val entryName: String)
