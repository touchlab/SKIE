package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector

class ObjCExportReflector(override val instance: Any) : Reflector(fqName) {

    var exportedInterface by declaredField<Any>()

    companion object {

        const val fqName: String = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExport"
    }
}
