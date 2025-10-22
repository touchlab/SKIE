package co.touchlab.skie.util

import org.jetbrains.kotlin.descriptors.Named
import org.jetbrains.kotlin.name.Name

fun String.collisionFreeIdentifier(existingIdentifiers: Collection<Named>): Name =
    collisionFreeIdentifier(existingIdentifiers.map { it.name.asString() }).let { Name.identifier(it) }
