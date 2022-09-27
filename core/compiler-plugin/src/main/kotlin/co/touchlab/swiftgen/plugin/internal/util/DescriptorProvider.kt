package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ObjCExportMapperReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ObjCExportReflector
import co.touchlab.swiftlink.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.module

internal class DescriptorProvider(private val context: CommonBackendContext) {

    val exportedModules: Set<ModuleDescriptor> by lazy {
        context.getAllExportedModuleDescriptors().toSet()
    }

    val classDescriptors: Set<ClassDescriptor> by lazy {
        exportedInterface.generatedClasses.filter { it.module in exportedModules }.toSet()
    }

    val mapper: ObjCExportMapperReflector by lazy {
        exportedInterface.reflectedMapper
    }

    private val exportedInterface by lazy {
        val objCExport = ObjCExportReflector.new(context)

        objCExport.reflectedExportedInterface
    }
}
