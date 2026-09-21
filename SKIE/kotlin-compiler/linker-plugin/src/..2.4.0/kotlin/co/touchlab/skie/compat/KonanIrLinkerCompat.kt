@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal val KonanIrLinker.modulesStringKeyed: Map<String, IrModuleFragment>
    get() = modules
