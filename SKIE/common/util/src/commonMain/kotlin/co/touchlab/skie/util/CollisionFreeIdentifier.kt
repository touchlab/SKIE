package co.touchlab.skie.util

fun String.collisionFreeIdentifier(existingIdentifiers: Collection<String>): String {
    val set = existingIdentifiers.toSet()

    return createCollisionFreeString(this) { it in set }
}

private tailrec fun createCollisionFreeString(baseString: String, collides: (String) -> Boolean): String =
    if (!collides(baseString)) baseString else createCollisionFreeString("${baseString}_", collides)
