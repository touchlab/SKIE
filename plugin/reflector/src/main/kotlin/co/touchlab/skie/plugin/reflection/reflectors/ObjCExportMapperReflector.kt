@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.konan.objcexport.CustomTypeMapper
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

// context()
internal class ObjCExportMapperReflector(override val instance: Any) : Reflector(
    "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper"
) {

    private val extensionClass = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt"

    val getCustomTypeMapper by declaredMethod1<ClassDescriptor, CustomTypeMapper?>()

    val isBaseMethod by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    val isBaseProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val isObjCProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val doesThrow by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    private val shouldBeExposedMember by extensionFunction1<CallableMemberDescriptor, Boolean>(extensionClass, "shouldBeExposed")

    private val shouldBeExposedClass by extensionFunction1<ClassDescriptor, Boolean>(extensionClass, "shouldBeExposed")

    val bridgeMethod by declaredMethod1<FunctionDescriptor, MethodBridge>()

    val bridgeType by extensionFunction1<KotlinType, TypeBridge>(extensionClass)

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean {
        return shouldBeExposedMember(descriptor)
    }

    fun shouldBeExposed(descriptor: ClassDescriptor): Boolean {
        return shouldBeExposedClass(descriptor)
    }
}

// context(MethodBridge)
// val receiver: MethodBridgeReceiver by Test()
//
// private val OptimizedImplementation.test: String
//     get() = "test"
//
// // context(MethodBridge)
// internal class Test<T: Any, R>: PropertyDelegateProvider<Nothing?, ReadOnlyProperty<T, R>> {
//     override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<T, R> {
//         return object: ReadOnlyProperty<T, R> {
//             override fun getValue(thisRef: T, property: KProperty<*>): R {
//                 thisRef.javaClass.getMethod(property.getter.name).apply {
//                     isAccessible = true
//                     return invoke(thisRef) as R
//                 }
//             }
//         }
//     }
// }
