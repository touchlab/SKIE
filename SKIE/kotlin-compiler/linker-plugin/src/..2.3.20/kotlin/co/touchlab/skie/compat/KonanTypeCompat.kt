@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

/**
 * Compiler-internal types using their pre-2.4.0 names.
 *
 * See the matching aliases in the `2.4.0..` source set for the names introduced in Kotlin 2.4.0.
 */
internal typealias KonanConfig = org.jetbrains.kotlin.backend.konan.KonanConfig

internal typealias KonanConfigKeys = org.jetbrains.kotlin.backend.konan.KonanConfigKeys

internal typealias PhaseContext = org.jetbrains.kotlin.backend.konan.driver.PhaseContext
