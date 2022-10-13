package co.touchlab.swiftlink.plugin

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

val CommonBackendContext.konanConfig: KonanConfig
    get() = javaClass.getMethod("getConfig").invoke(this) as KonanConfig

val CommonBackendContext.objcExportNamer: ObjCExportNamer?
    get() = javaClass.getMethod("getObjCExport").invoke(this).let {
        it.javaClass.getField("namer").get(it) as ObjCExportNamer
    }

private val CommonBackendContext.compilerOutputField: java.lang.reflect.Field
    get() = javaClass.getField("compilerOutput")

var CommonBackendContext.compilerOutput: List<ObjectFile>
    get() = compilerOutputField.get(this) as? List<ObjectFile> ?: emptyList()
    set(value) { compilerOutputField.set(this, value) }

val CommonBackendContext.moduleDescriptor: ModuleDescriptor?
    get() = javaClass.getMethod("getModuleDescriptor").invoke(this) as? ModuleDescriptor

fun CommonBackendContext.getExportedDependencies(): List<ModuleDescriptor> {
    val method = Class.forName("org.jetbrains.kotlin.backend.konan.FeaturedLibrariesKt")
        .getMethod("getExportedDependencies", Class.forName("org.jetbrains.kotlin.backend.konan.Context"))
    return method.invoke(null, this) as? List<ModuleDescriptor> ?: emptyList()
}

fun CommonBackendContext.getAllExportedModuleDescriptors(): List<ModuleDescriptor> {
    return listOfNotNull(moduleDescriptor) + getExportedDependencies()
}
