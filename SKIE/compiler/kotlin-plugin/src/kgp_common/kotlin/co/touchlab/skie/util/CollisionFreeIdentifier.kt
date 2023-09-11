package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.Named
import org.jetbrains.kotlin.name.Name

fun String.collisionFreeIdentifier(existingIdentifiers: Collection<String>): String {
    val set = existingIdentifiers.toSet()

    return createCollisionFreeString(this) { it in set }
}

fun String.collisionFreeIdentifier(existingIdentifiers: Collection<Named>): Name =
    collisionFreeIdentifier(existingIdentifiers.map { it.name.asString() }).let { Name.identifier(it) }

private tailrec fun createCollisionFreeString(baseString: String, collides: (String) -> Boolean): String =
    if (!collides(baseString)) baseString else createCollisionFreeString("${baseString}_", collides)

