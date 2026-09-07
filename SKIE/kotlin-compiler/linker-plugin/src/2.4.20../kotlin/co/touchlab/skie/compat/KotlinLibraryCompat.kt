@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.library.KotlinLibrary

/**
 * Kotlin 2.4.20 removed `KotlinLibrary.libraryFile` in favour of `Klib.path` (`java.nio.file.Path`).
 */
internal val KotlinLibrary.libraryFilePath: String
    get() = path.toString()
