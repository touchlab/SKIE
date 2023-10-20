package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.serialization.KonanIdSignaturer
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.SymbolTable

class ObjCExportReflector(
    override val instance: Any,
) : Reflector(fqName) {

    var exportedInterface by declaredField<Any>()

    companion object {

        const val fqName: String = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExport"

        fun new(context: CommonBackendContext): ObjCExportReflector {
            val aClass = this::class.java.classLoader.loadClass(fqName)

            val constructor = aClass.constructors.first()

            return constructor.newInstance(context, createSymbolTable()).reflectedBy()
        }

        private fun createSymbolTable(): SymbolTable =
            SymbolTable(KonanIdSignaturer(KonanManglerDesc), IrFactoryImpl)
    }
}
