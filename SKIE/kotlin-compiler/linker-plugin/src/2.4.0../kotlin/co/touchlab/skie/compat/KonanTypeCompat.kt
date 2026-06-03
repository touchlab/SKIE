@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

/**
 * Compiler-internal types that were renamed in Kotlin 2.4.0.
 *
 * See the matching aliases in the `..2.3.20` source set for the pre-2.4.0 names.
 */
internal typealias KonanConfig = org.jetbrains.kotlin.backend.konan.NativeSecondStageCompilationConfig

internal typealias KonanConfigKeys = org.jetbrains.kotlin.konan.config.NativeConfigurationKeys

internal typealias PhaseContext = org.jetbrains.kotlin.backend.konan.driver.NativePhaseContext
