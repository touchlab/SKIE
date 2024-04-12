package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.configuration.FileConfiguration
import co.touchlab.skie.configuration.FileOrClassConfiguration
import co.touchlab.skie.configuration.ModuleConfiguration
import co.touchlab.skie.configuration.PackageConfiguration
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.element.superClass
import co.touchlab.skie.kir.type.DeclarationBackedKirType
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.kir.util.hasArgumentValue
import co.touchlab.skie.oir.element.OirTypeParameter
import co.touchlab.skie.phases.DescriptorConversionPhase
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isSealed
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.Variance

class CreateExposedKirTypesPhase(
    context: DescriptorConversionPhase.Context,
) : DescriptorConversionPhase {

    private val descriptorProvider = context.descriptorProvider
    private val kirProvider = context.kirProvider
    private val descriptorKirProvider = context.descriptorKirProvider
    private val kirBuiltins = context.kirBuiltins
    private val namer = context.namer
    private val descriptorConfigurationProvider = context.descriptorConfigurationProvider
    private val rootConfiguration = context.rootConfiguration
    private val kirTypeTranslator = context.kirTypeTranslator

    private val descriptorsToClasses = mutableMapOf<ClassDescriptor, KirClass>()

    private val sourceFileConfiguration = ClassConfiguration(
        FileOrClassConfiguration.File(
            FileConfiguration(PackageConfiguration(ModuleConfiguration(rootConfiguration))),
        ),
    )

    context(DescriptorConversionPhase.Context)
    override suspend fun execute() {
        createRegularClasses()
        createFileClasses()
        configureSealedSubclasses()

        configureSuperTypes()

        kirProvider.initializeKotlinClassCache()
    }

    private fun createRegularClasses() {
        descriptorProvider.exposedClasses.forEach(::getOrCreateClass)
    }

    private fun createFileClasses() {
        descriptorProvider.exposedFiles.forEach(::createClass)
    }

    private fun getOrCreateClass(descriptor: ClassDescriptor): KirClass =
        descriptorsToClasses.getOrPut(descriptor) {
            createClass(descriptor)
        }

    private fun createClass(descriptor: ClassDescriptor): KirClass {
        val parent = (descriptor.containingDeclaration as? ClassDescriptor)
            ?.takeIf { it in descriptorProvider.exposedClasses }
            ?.let { getOrCreateClass(it) }
            ?: descriptorKirProvider.getKotlinModule(descriptor.module)

        val classOrProtocolName = namer.getClassOrProtocolName(descriptor)

        val kirClass = KirClass(
            kotlinFqName = descriptor.fqNameSafe.asString(),
            objCName = classOrProtocolName.objCName,
            swiftName = classOrProtocolName.swiftName,
            parent = parent,
            kind = when (descriptor.kind) {
                ClassKind.CLASS -> KirClass.Kind.Class
                ClassKind.INTERFACE -> KirClass.Kind.Interface
                ClassKind.ENUM_CLASS -> KirClass.Kind.Enum
                ClassKind.OBJECT -> if (descriptor.isCompanionObject) KirClass.Kind.CompanionObject else KirClass.Kind.Object
                ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> error("Unexpected class kind: ${descriptor.kind}")
            },
            origin = KirClass.Origin.Kotlin,
            superTypes = emptyList(),
            isSealed = descriptor.isSealed(),
            hasUnexposedSealedSubclasses = descriptor.sealedSubclasses.any { !it.isExposed },
            configuration = descriptorConfigurationProvider.getConfiguration(descriptor),
        )

        descriptorKirProvider.registerClass(kirClass, descriptor)

        configureClassParent(kirClass)

        with(descriptorKirProvider) {
            createTypeParameters(kirClass, descriptor, namer)
        }

        createEnumEntries(kirClass, descriptor)

        return kirClass
    }

    private fun createEnumEntries(kirClass: KirClass, descriptor: ClassDescriptor) {
        if (kirClass.kind != KirClass.Kind.Enum) {
            return
        }

        descriptor.enumEntries.forEachIndexed { index, classDescriptor ->
            val originalDescriptor = classDescriptor.original

            val enumEntry = KirEnumEntry(
                kotlinName = classDescriptor.name.asString(),
                objCSelector = namer.getEnumEntrySelector(originalDescriptor),
                swiftName = namer.getEnumEntrySwiftName(originalDescriptor),
                owner = kirClass,
                index = index,
                hasUserDefinedName = classDescriptor.hasUserDefinedName,
            )

            descriptorKirProvider.registerEnumEntry(enumEntry, classDescriptor)
        }
    }

    private fun configureClassParent(kirClass: KirClass) {
        if (kirClass.parent is KirClass) {
            if (kirClass.kind == KirClass.Kind.CompanionObject) {
                kirClass.parent.companionObject = kirClass
            }
        }
    }

    private fun createClass(file: SourceFile) {
        val module = descriptorKirProvider.getKotlinModule(descriptorProvider.getFileModule(file))

        val fileClassName = namer.getFileClassName(file)

        val kirClass = KirClass(
            kotlinFqName = fileClassName.swiftName,
            objCName = fileClassName.objCName,
            swiftName = fileClassName.swiftName,
            parent = module,
            kind = KirClass.Kind.File,
            origin = KirClass.Origin.Kotlin,
            superTypes = listOf(kirBuiltins.Base.defaultType),
            isSealed = false,
            hasUnexposedSealedSubclasses = false,
            configuration = sourceFileConfiguration,
        )

        descriptorKirProvider.registerFile(kirClass, file)
    }

    private fun configureSealedSubclasses() {
        descriptorsToClasses.forEach {
            configureSealedSubclasses(it.value, it.key)
        }
    }

    private fun configureSealedSubclasses(kirClass: KirClass, classDescriptor: ClassDescriptor) {
        kirClass.sealedSubclasses.addAll(
            classDescriptor.sealedSubclasses.mapNotNull { descriptorsToClasses[it] },
        )
    }

    private fun configureSuperTypes() {
        descriptorsToClasses.forEach {
            configureSuperTypes(it.value, it.key)
        }
    }

    private fun configureSuperTypes(kirClass: KirClass, classDescriptor: ClassDescriptor) {
        kirClass.withTypeParameterScope {
            val superTypesWithoutAny = classDescriptor.defaultType.constructor.supertypes
                .filter { !KotlinBuiltIns.isAnyOrNullableAny(it) }
                .map { kirTypeTranslator.mapReferenceType(it) }
                .filterIsInstance<DeclarationBackedKirType>()

            kirClass.superTypes.addAll(superTypesWithoutAny)

            if (kirClass.kind != KirClass.Kind.Interface && kirClass.superClass == null) {
                kirClass.superTypes.add(kirBuiltins.Base.defaultType)
            }
        }
    }

    private val ClassDescriptor.isExposed: Boolean
        get() = this in descriptorProvider.exposedClasses

    private val ClassDescriptor.hasUserDefinedName: Boolean
        get() = this.annotations.findAnnotation(KonanFqNames.objCName)
            ?.let { !it.hasArgumentValue("name") && !it.hasArgumentValue("swiftName") }
            ?: true

    companion object {

        context(DescriptorKirProvider)
        fun createTypeParameters(kirClass: KirClass, descriptor: ClassDescriptor, namer: ObjCExportNamer) {
            if (kirClass.kind != KirClass.Kind.Class) {
                return
            }

            descriptor.typeConstructor.parameters.forEach { typeParameter ->
                createTypeParameter(kirClass, typeParameter, namer)
            }
        }

        context(DescriptorKirProvider)
        private fun createTypeParameter(
            kirClass: KirClass,
            typeParameterDescriptor: TypeParameterDescriptor,
            namer: ObjCExportNamer,
        ) {
            val typeParameter = KirTypeParameter(
                name = namer.getTypeParameterName(typeParameterDescriptor),
                parent = kirClass,
                variance = when (typeParameterDescriptor.variance) {
                    Variance.INVARIANT -> OirTypeParameter.Variance.Invariant
                    Variance.IN_VARIANCE -> OirTypeParameter.Variance.Contravariant
                    Variance.OUT_VARIANCE -> OirTypeParameter.Variance.Covariant
                },
                // Bounds are not supported.
            )

            this@DescriptorKirProvider.registerTypeParameter(typeParameter, typeParameterDescriptor)
        }
    }
}
