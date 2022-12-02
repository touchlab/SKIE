package co.touchlab.skie.plugin.generator.internal.util

internal tailrec fun createCollisionFreeString(baseString: String, collides: (String) -> Boolean): String =
    if (!collides(baseString)) baseString else createCollisionFreeString("_$baseString", collides)
