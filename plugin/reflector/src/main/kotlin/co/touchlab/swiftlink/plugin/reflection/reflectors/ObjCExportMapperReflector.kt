@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.Reflector
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeReceiver
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberScope.Implementation
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberScope.OptimizedImplementation
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge

// context()
internal class ObjCExportMapperReflector(override val instance: Any) : Reflector(
    "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper"
) {

    private val extensionClass = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt"

    val isBaseMethod by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    val isBaseProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val isObjCProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val doesThrow by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    val shouldBeExposed by extensionFunction1<CallableMemberDescriptor, Boolean>(extensionClass)

    val bridgeMethod by declaredMethod1<FunctionDescriptor, MethodBridge>()

    val bridgeType by extensionFunction1<KotlinType, TypeBridge>(extensionClass)
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
