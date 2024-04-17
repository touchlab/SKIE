@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

class ObjCExportedInterfaceProvider internal constructor(
    internal val objCExportedInterface: ObjCExportedInterface,
) {

    val namer: ObjCExportNamer
        get() = objCExportedInterface.namer
}
