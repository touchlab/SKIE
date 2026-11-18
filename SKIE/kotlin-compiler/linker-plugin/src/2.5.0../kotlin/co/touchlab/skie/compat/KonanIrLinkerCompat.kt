@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.common.serialization.kotlinLibrary
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal val KonanIrLinker.modulesStringKeyed: Map<String, IrModuleFragment>
    get() = allModuleFragments
        .mapNotNull { module -> module.kotlinLibrary?.let { it.libraryFilePath to module } }
        .toMap()
