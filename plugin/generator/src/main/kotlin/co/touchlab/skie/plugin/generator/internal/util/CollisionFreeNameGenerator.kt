package co.touchlab.skie.plugin.generator.internal.util

import org.jetbrains.kotlin.descriptors.Named
import org.jetbrains.kotlin.name.Name

internal tailrec fun createCollisionFreeString(baseString: String, collides: (String) -> Boolean): String =
    if (!collides(baseString)) baseString else createCollisionFreeString("_$baseString", collides)

internal fun List<Named>.createCollisionFreeIdentifier(baseString: String): Name {
    val names = this.map { it.name.asString() }

    val newName = createCollisionFreeString(baseString) { it in names }

    return Name.identifier(newName)
}

