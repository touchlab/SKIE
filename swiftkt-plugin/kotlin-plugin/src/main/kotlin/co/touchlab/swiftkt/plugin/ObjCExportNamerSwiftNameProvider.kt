package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spi.SwiftNameProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName

class ObjCExportNamerSwiftNameProvider(
    private val namer: ObjCExportNamer,
    private val context: CommonBackendContext,
): SwiftNameProvider {
    override fun getSwiftName(kotlinClassName: String): String {
        val descriptor = checkNotNull(context.ir.irModule.descriptor.resolveClassByFqName(
            FqName(kotlinClassName),
            NoLookupLocation.FROM_BACKEND
        )) {
            "Couldn't resolve class descriptor for $kotlinClassName"
        }

        val swiftName = namer.getClassOrProtocolName(descriptor)

        return swiftName.swiftName
    }
}
