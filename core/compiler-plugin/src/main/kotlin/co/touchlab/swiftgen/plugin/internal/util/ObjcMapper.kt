package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ObjcMapper(private val mapper: Any) {
    private val fileClass = this::class.java.classLoader.loadClass("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt")

    private val isBaseMethodRef = fileClass.getDeclaredMethod("isBaseMethod", mapper.javaClass, FunctionDescriptor::class.java)
    fun isBaseMethod(method: FunctionDescriptor): Boolean {
        return isBaseMethodRef.invoke(null, mapper, method) as Boolean
    }

    private val isBasePropertyRef = fileClass.getDeclaredMethod("isBaseProperty", mapper.javaClass, PropertyDescriptor::class.java)
    fun isBaseProperty(property: PropertyDescriptor): Boolean {
        return isBasePropertyRef.invoke(null, mapper, property) as Boolean
    }

    private val doesThrowRef = fileClass.getDeclaredMethod("doesThrow", mapper.javaClass, FunctionDescriptor::class.java)
    fun doesThrow(method: FunctionDescriptor): Boolean {
        return doesThrowRef.invoke(null, mapper, method) as Boolean
    }

}
