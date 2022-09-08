package co.touchlab.swiftgen.plugin.internal.util

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class DescriptorProvider(private val context: CommonBackendContext) {

    val classDescriptors: Set<ClassDescriptor> by lazy {
        val getter = exportedInterfaceClass.getDeclaredMethod("getGeneratedClasses")

        @Suppress("UNCHECKED_CAST")
        getter.invoke(exportedInterface) as Set<ClassDescriptor>
    }

    private val exportedInterface: Any by lazy {
        val exportObject = getObjcExport()

        val field = exportClass.getDeclaredField("exportedInterface")

        field.isAccessible = true

        field.get(exportObject)
    }

    private val exportClass: Class<*> =
        this::class.java.classLoader
            .loadClass("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExport")

    private val exportedInterfaceClass: Class<*> =
        this::class.java.classLoader
            .loadClass("org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface")

    private fun getObjcExport(): Any {
        val constructor = exportClass.constructors.first()

        return constructor.newInstance(context, createSymbolTable())
    }

    private fun createSymbolTable(): SymbolTable =
        SymbolTable(DummySignaturer(), IrFactoryImpl)

    private class DummySignaturer : IdSignatureComposer {

        override fun composeAnonInitSignature(descriptor: ClassDescriptor): IdSignature? = null

        override fun composeEnumEntrySignature(descriptor: ClassDescriptor): IdSignature? = null

        override fun composeFieldSignature(descriptor: PropertyDescriptor): IdSignature? = null

        override fun composeSignature(descriptor: DeclarationDescriptor): IdSignature? = null

        override fun withFileSignature(fileSignature: IdSignature.FileSignature, body: () -> Unit) {
            body()
        }
    }
}