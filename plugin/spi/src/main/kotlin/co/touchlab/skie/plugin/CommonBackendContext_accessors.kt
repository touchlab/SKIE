@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

val CommonBackendContext.moduleDescriptorOrNull: ModuleDescriptor?
    get() = (this as? KonanContext)?.moduleDescriptor

fun CommonBackendContext.getAllExportedModuleDescriptors(): List<ModuleDescriptor> {
    return if (this is KonanContext) {
        listOfNotNull(moduleDescriptor) + getExportedDependencies()
    } else {
        emptyList()
    }
}
