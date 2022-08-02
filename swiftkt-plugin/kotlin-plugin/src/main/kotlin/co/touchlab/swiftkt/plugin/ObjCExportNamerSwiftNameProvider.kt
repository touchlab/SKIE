package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KobjcTransforms
import co.touchlab.swiftpack.spi.SwiftNameProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName

class ObjCExportNamerSwiftNameProvider(
    private val namer: ObjCExportNamer,
    private val context: CommonBackendContext,
    private val transforms: KobjcTransforms,
): SwiftNameProvider {

    val renamedClasses: Map<String, ObjCExportNamer.ClassOrProtocolName>

    init {
        val renamedClasses = mutableMapOf<String, ObjCExportNamer.ClassOrProtocolName>()
        transforms.types.values.forEach { transform ->
            val name = namer.getClassOrProtocolName(resolveClass(transform.type))
            when {
                transform.hide -> {
                    renamedClasses[transform.type] = name.copy(swiftName = "__${name.swiftName}")
                }
                transform.rename != null -> {
                    renamedClasses[transform.type] = name.copy(swiftName = transform.rename!!)
                }
            }
        }
        this.renamedClasses = renamedClasses
    }

    override fun getSwiftName(kotlinClassName: String): String = getClassOrProtocolName(kotlinClassName).swiftName

    fun getObjCName(kotlinClassName: String): String = getClassOrProtocolName(kotlinClassName).objCName

    fun getClassOrProtocolName(kotlinClassName: String): ObjCExportNamer.ClassOrProtocolName {
        return renamedClasses.getOrElse(kotlinClassName) { namer.getClassOrProtocolName(resolveClass(kotlinClassName)) }
    }

    private fun resolveClass(kotlinClassName: String): ClassDescriptor {
        return checkNotNull(context.ir.irModule.descriptor.resolveClassByFqName(
            FqName(kotlinClassName),
            NoLookupLocation.FROM_BACKEND
        )) {
            "Couldn't resolve class descriptor for $kotlinClassName"
        }
    }
}
