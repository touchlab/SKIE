@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.util

import org.jetbrains.kotlin.backend.common.serialization.extractSerializedKdocString
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

/**
 * Extracts the KDoc of a Kotlin declaration and returns it as plain documentation
 * content (no `/** */` delimiters, no leading `*`), suitable for handing to the
 * Swift code generator which re-wraps it into a Swift doc comment.
 *
 * At framework-linking time declarations are deserialized from klibs, so the KDoc
 * is read from the serialized klib metadata — the same source the Obj-C header uses.
 *
 * Fixes touchlab/SKIE#166.
 */
internal fun DeclarationDescriptor.extractDocumentationOrNull(): String? {
    val rawKDoc = extractSerializedKdocString() ?: return null

    return rawKDoc.cleanKDocContent().takeIf { it.isNotBlank() }
}

private fun String.cleanKDocContent(): String {
    var text = trim()

    if (text.startsWith("/**")) {
        text = text.removePrefix("/**")
    }
    if (text.endsWith("*/")) {
        text = text.removeSuffix("*/")
    }

    return text
        .lines()
        .joinToString("\n") { line ->
            // Drop the leading ` * ` (or `*`) margin that KDoc lines conventionally carry.
            line.trimStart().removePrefix("*").let { if (it.startsWith(" ")) it.drop(1) else it }
        }
        .trim('\n')
        .trimEnd()
}
