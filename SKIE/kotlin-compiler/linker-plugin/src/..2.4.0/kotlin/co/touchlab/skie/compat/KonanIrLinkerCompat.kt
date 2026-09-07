@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * Kotlin < 2.4.20 keys `KonanIrLinker.modules` by the klib path (`String`).
 */
internal val KonanIrLinker.modulesStringKeyed: Map<String, IrModuleFragment>
    get() = modules
