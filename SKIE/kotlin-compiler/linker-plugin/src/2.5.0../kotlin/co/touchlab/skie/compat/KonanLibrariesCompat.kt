@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.klibModuleOriginOrNull

internal val KonanConfig.skieResolvedLibraries: List<KotlinLibrary>
    get() = loadedKlibs.all

internal val ModuleDescriptor.skieKonanLibrary: KotlinLibrary?
    get() = (klibModuleOriginOrNull as? DeserializedKlibModuleOrigin)?.library
