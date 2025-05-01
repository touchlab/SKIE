@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportScope
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportTranslatorImpl
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.types.KotlinType

class ObjCExportTranslatorImplReflector(override val instance: ObjCExportTranslator) : Reflector(ObjCExportTranslatorImpl::class) {

    internal val mapType by declaredMethod3<KotlinType, TypeBridge, ObjCExportScope, ObjCType>()
}
