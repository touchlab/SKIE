package co.touchlab.skie.util

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

private val json = Json { prettyPrint = true }

inline fun <reified T> T.toPrettyJson(): String =
    toPrettyJson(serializer())

fun <T> T.toPrettyJson(serializer: SerializationStrategy<T>): String =
    json.encodeToString(serializer, this)
