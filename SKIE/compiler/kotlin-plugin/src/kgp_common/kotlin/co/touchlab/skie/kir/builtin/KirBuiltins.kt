@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.builtin

import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.type.ReferenceKirType
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("PropertyName", "FunctionName")
class KirBuiltins(
    val stdlibModule: KirModule,
    private val kotlinBuiltIns: KotlinBuiltIns,
    private val extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    private val namer: ObjCExportNamer,
) {
    // TODO Builtin methods are not supported yet

    val Base by Class(
        name = namer.kotlinAnyName,
        classDescriptor = kotlinBuiltIns.any,
        superTypes = listOf(
            extraDescriptorBuiltins.NSObject.defaultType,
            extraDescriptorBuiltins.NSCopying.defaultType,
        ),
    )

    val MutableSet by Class(
        name = namer.mutableSetName,
        classDescriptor = kotlinBuiltIns.mutableSet,
    ) {
        val typeArgument = TypeProjectionImpl(kotlinBuiltIns.mutableSet.declaredTypeParameters.single().defaultType)

        val kotlinSuperType = KotlinTypeFactory.simpleType(extraDescriptorBuiltins.NSMutableSet.defaultType, arguments = listOf(typeArgument))

        superTypes.add(
            ReferenceKirType(kotlinSuperType),
        )
    }

    val MutableMap: KirClass by Class(
        name = namer.mutableMapName,
        classDescriptor = kotlinBuiltIns.mutableMap,
    ) {
        val keyArgument = TypeProjectionImpl(kotlinBuiltIns.mutableMap.declaredTypeParameters[0].defaultType)
        val valueArgument = TypeProjectionImpl(kotlinBuiltIns.mutableMap.declaredTypeParameters[1].defaultType)

        val kotlinSuperType = KotlinTypeFactory.simpleType(
            baseType = extraDescriptorBuiltins.NSMutableDictionary.defaultType,
            arguments = listOf(keyArgument, valueArgument),
        )

        superTypes.add(
            ReferenceKirType(kotlinSuperType),
        )
    }

    val Number: KirClass by Class(
        name = namer.kotlinNumberName,
        classDescriptor = kotlinBuiltIns.number,
        superTypes = listOf(
            extraDescriptorBuiltins.NSNumber.defaultType,
        ),
    )

    val nsNumberDeclarations: Map<ClassId, KirClass> =
        NSNumberKind.values().mapNotNull { it.mappedKotlinClassId }
            .associateWith { classId ->
                val descriptor = kotlinBuiltIns.getBuiltInClassByFqName(FqName(classId.asFqNameString()))

                KirClass(
                    descriptor = KirClass.Descriptor.Class(descriptor),
                    name = namer.numberBoxName(classId),
                    parent = stdlibModule,
                    kind = KirClass.Kind.Class,
                    superTypes = listOf(kotlinBuiltIns.numberType).map(::ReferenceKirType),
                    isSealed = false,
                    hasUnexposedSealedSubclasses = false,
                    belongsToSkieKotlinRuntime = false,
                )
            }

    private fun Class(
        name: ObjCExportNamer.ClassOrProtocolName,
        classDescriptor: ClassDescriptor,
        superTypes: List<KotlinType> = emptyList(),
        apply: KirClass.() -> Unit = {},
    ): ReadOnlyProperty<Any?, KirClass> =
        object : ReadOnlyProperty<Any?, KirClass> {

            private val value = KirClass(
                descriptor = KirClass.Descriptor.Class(classDescriptor),
                name = name,
                parent = stdlibModule,
                kind = KirClass.Kind.Class,
                superTypes = superTypes.map(::ReferenceKirType),
                isSealed = false,
                hasUnexposedSealedSubclasses = false,
                belongsToSkieKotlinRuntime = false,
            ).apply {
                classDescriptor.declaredTypeParameters.forEach { typeParameter ->
                    KirTypeParameter(typeParameter, this)
                }

                apply()
            }

            override fun getValue(thisRef: Any?, property: KProperty<*>): KirClass =
                value
        }
}
