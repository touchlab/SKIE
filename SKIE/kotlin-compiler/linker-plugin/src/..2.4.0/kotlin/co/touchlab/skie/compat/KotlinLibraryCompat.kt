@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.library.KotlinLibrary

/**
 * Kotlin < 2.4.20 exposes the library location as `KotlinLibrary.libraryFile` (`java.io.File`).
 */
internal val KotlinLibrary.libraryFilePath: String
    get() = libraryFile.absolutePath
