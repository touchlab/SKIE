@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.konan.ir.konanLibrary
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.library.KotlinLibrary

internal val KonanConfig.skieResolvedLibraries: List<KotlinLibrary>
    get() = resolvedLibraries.getFullList()

internal val ModuleDescriptor.skieKonanLibrary: KotlinLibrary?
    get() = konanLibrary
