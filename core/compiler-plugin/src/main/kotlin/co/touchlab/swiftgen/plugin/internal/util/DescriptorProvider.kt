package co.touchlab.swiftgen.plugin.internal.util

import co.touchlab.swiftlink.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.IdSignatureComposer
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.resolve.descriptorUtil.module

internal class DescriptorProvider(private val context: CommonBackendContext) {

    val exportedModules: List<ModuleDescriptor> by lazy {
        context.getAllExportedModuleDescriptors()
    }
    val allClassDescriptors: Set<ClassDescriptor> by lazyMethod("getGeneratedClasses")
    val classDescriptors: Set<ClassDescriptor> by lazy {
        val exportedModulesSet = exportedModules.toSet()
        allClassDescriptors.filter { it.module in exportedModulesSet }.toSet()
    }
    val categoryMembers: Map<ClassDescriptor, List<CallableMemberDescriptor>> by lazyMethod("getCategoryMembers")
    val mapper: ObjcMapper by lazy {
        val mapper = lazyMethod<Any>("getMapper").value
        ObjcMapper(mapper)
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

    private fun <T> lazyMethod(name: String): Lazy<T> = lazy {
        val getter = exportedInterfaceClass.getDeclaredMethod(name)

        @Suppress("UNCHECKED_CAST")
        getter.invoke(exportedInterface) as T
    }

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
