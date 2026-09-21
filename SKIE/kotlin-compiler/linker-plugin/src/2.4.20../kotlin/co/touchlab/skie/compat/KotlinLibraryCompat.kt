@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.library.KotlinLibrary

internal val KotlinLibrary.libraryFilePath: String
    get() = path.toString()
