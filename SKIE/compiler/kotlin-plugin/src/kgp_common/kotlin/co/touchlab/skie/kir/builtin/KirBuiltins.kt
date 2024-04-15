@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.builtin

import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.element.toTypeParameterUsage
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.oir.element.OirTypeParameter
import co.touchlab.skie.phases.kir.CreateExposedKirTypesPhase
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

@Suppress("PropertyName", "FunctionName")
class KirBuiltins(
    val stdlibModule: KirModule,
    kotlinBuiltIns: KotlinBuiltIns,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    private val namer: ObjCExportNamer,
    private val descriptorConfigurationProvider: DescriptorConfigurationProvider,
    private val descriptorKirProvider: DescriptorKirProvider,
) {
    // TODO Not all Builtin methods are supported yet (supported are only those converted from Kotlin equivalents in Any)

    private val mutableBuiltinClasses = mutableSetOf<KirClass>()

    val builtinClasses: Set<KirClass> by ::mutableBuiltinClasses

    val NSObject: KirClass = Class(extraDescriptorBuiltins.NSObject)

    val NSCopying: KirClass = Class(extraDescriptorBuiltins.NSCopying)

    val NSError: KirClass = Class(extraDescriptorBuiltins.NSError) {
        superTypes.add(NSObject.defaultType)
    }

    val NSString: KirClass = Class(extraDescriptorBuiltins.NSString) {
        superTypes.add(NSObject.defaultType)
    }

    val NSArray: KirClass = Class(extraDescriptorBuiltins.NSArray) {
        KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        superTypes.add(NSObject.defaultType)
    }

    val NSMutableArray: KirClass = Class(extraDescriptorBuiltins.NSMutableArray) {
        val typeParameter = KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val typeArgument = typeParameter.toTypeParameterUsage()

        superTypes.add(
            NSArray.toType(typeArgument),
        )
    }

    val NSSet: KirClass = Class(extraDescriptorBuiltins.NSSet) {
        KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        superTypes.add(NSObject.defaultType)
    }

    val NSMutableSet: KirClass = Class(extraDescriptorBuiltins.NSMutableSet) {
        val typeParameter = KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val typeArgument = typeParameter.toTypeParameterUsage()

        superTypes.add(
            NSSet.toType(typeArgument),
        )
    }

    val NSDictionary: KirClass = Class(extraDescriptorBuiltins.NSDictionary) {
        KirTypeParameter(
            name = "KeyType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        superTypes.add(NSObject.defaultType)
    }

    val NSMutableDictionary: KirClass = Class(extraDescriptorBuiltins.NSMutableDictionary) {
        val keyTypeParameter = KirTypeParameter(
            name = "KeyType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val valueTypeParameter = KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val keyTypeArgument = keyTypeParameter.toTypeParameterUsage()
        val valueTypeArgument = valueTypeParameter.toTypeParameterUsage()

        superTypes.add(
            NSDictionary.toType(keyTypeArgument, valueTypeArgument),
        )
    }

    val NSNumber: KirClass = Class(extraDescriptorBuiltins.NSNumber) {
        // Technically should be NSValue instead but for our purposes it's not needed because we use the class only as a supertype
        superTypes.add(NSObject.defaultType)
    }

    val Base: KirClass = Class(
        name = namer.kotlinAnyName,
        classDescriptor = kotlinBuiltIns.any,
        superTypes = listOf(
            NSObject.defaultType,
            NSCopying.defaultType,
        ),
    )

    val MutableSet: KirClass = Class(
        name = namer.mutableSetName,
        classDescriptor = kotlinBuiltIns.mutableSet,
    ) {
        val typeParameter = KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val typeArgument = typeParameter.toTypeParameterUsage()

        superTypes.add(
            NSMutableSet.toType(typeArgument),
        )
    }

    val MutableMap: KirClass = Class(
        name = namer.mutableMapName,
        classDescriptor = kotlinBuiltIns.mutableMap,
    ) {
        val keyTypeParameter = KirTypeParameter(
            name = "KeyType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val valueTypeParameter = KirTypeParameter(
            name = "ObjectType",
            parent = this,
            variance = OirTypeParameter.Variance.Covariant,
        )

        val keyTypeArgument = keyTypeParameter.toTypeParameterUsage()
        val valueTypeArgument = valueTypeParameter.toTypeParameterUsage()

        superTypes.add(
            NSMutableDictionary.toType(keyTypeArgument, valueTypeArgument),
        )
    }

    val Number: KirClass = Class(
        name = namer.kotlinNumberName,
        classDescriptor = kotlinBuiltIns.number,
        superTypes = listOf(
            NSNumber.defaultType,
        ),
    )

    val nsNumberDeclarations: Map<ClassId, KirClass> =
        NSNumberKind.values().mapNotNull { it.mappedKotlinClassId }
            .associateWith { classId ->
                Class(
                    name = namer.numberBoxName(classId),
                    classDescriptor = kotlinBuiltIns.getBuiltInClassByFqName(FqName(classId.asFqNameString())),
                    superTypes = listOf(Number.defaultType),
                )
            }

    private fun Class(
        name: ObjCExportNamer.ClassOrProtocolName,
        classDescriptor: ClassDescriptor,
        superTypes: List<DeclaredKirType> = emptyList(),
        apply: KirClass.() -> Unit = {},
    ): KirClass = KirClass(
        kotlinFqName = classDescriptor.fqNameSafe.asString(),
        objCName = name.objCName,
        swiftName = name.swiftName,
        parent = stdlibModule,
        kind = KirClass.Kind.Class,
        origin = KirClass.Origin.Kotlin,
        superTypes = superTypes,
        isSealed = false,
        hasUnexposedSealedSubclasses = false,
        nestingLevel = CreateExposedKirTypesPhase.getNestingLevel(classDescriptor),
        configuration = descriptorConfigurationProvider.getConfiguration(classDescriptor),
    ).apply {
        descriptorKirProvider.registerClass(this, classDescriptor)

        mutableBuiltinClasses.add(this)

        apply()
    }

    fun Class(descriptor: ClassDescriptor, apply: KirClass.() -> Unit = {}): KirClass =
        descriptorKirProvider.getExternalBuiltinClass(descriptor).apply {
            mutableBuiltinClasses.add(this)

            apply()
        }
}
