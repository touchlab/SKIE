package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.configuration.FileConfiguration
import co.touchlab.skie.configuration.FileOrClassConfiguration
import co.touchlab.skie.configuration.ModuleConfiguration
import co.touchlab.skie.configuration.PackageConfiguration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.runtime.belongsToSkieKotlinRuntime
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isSealed
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class CreateKirTypesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val descriptorProvider = context.descriptorProvider
    private val kirProvider = context.kirProvider
    private val kotlinBuiltins = context.kotlinBuiltins
    private val namer = context.namer
    private val descriptorConfigurationProvider = context.descriptorConfigurationProvider
    private val rootConfiguration = context.rootConfiguration

    private val baseType = ReferenceKirType(kotlinBuiltins.anyType)

    private val descriptorsToClasses = mutableMapOf<ClassDescriptor, KirClass>()

    private val sourceFileConfiguration = ClassConfiguration(
        FileOrClassConfiguration.File(
            FileConfiguration(PackageConfiguration(ModuleConfiguration(rootConfiguration))),
        ),
    )

    context(SirPhase.Context)
    override suspend fun execute() {
        createRegularClasses()
        createFileClasses()
        configureSealedSubclasses()

        kirProvider.initializeClassCache()
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
            ?: kirProvider.getModule(descriptor.module)

        val kirClass = KirClass(
            descriptor = KirClass.Descriptor.Class(descriptor.original),
            name = namer.getClassOrProtocolName(descriptor),
            parent = parent,
            kind = when (descriptor.kind) {
                ClassKind.CLASS -> KirClass.Kind.Class
                ClassKind.INTERFACE -> KirClass.Kind.Interface
                ClassKind.ENUM_CLASS -> KirClass.Kind.Enum
                ClassKind.OBJECT -> if (descriptor.isCompanionObject) KirClass.Kind.CompanionObject else KirClass.Kind.Object
                ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> error("Unexpected class kind: ${descriptor.kind}")
            },
            superTypes = descriptor.defaultType.constructor.supertypes.map { ReferenceKirType(it) },
            isSealed = descriptor.isSealed(),
            hasUnexposedSealedSubclasses = descriptor.sealedSubclasses.any { !it.isExposed },
            belongsToSkieKotlinRuntime = descriptor.belongsToSkieKotlinRuntime,
            configuration = descriptorConfigurationProvider.getConfiguration(descriptor),
        )

        configureClassParent(kirClass)

        createTypeParameters(kirClass, descriptor)

        createEnumEntries(kirClass, descriptor)

        return kirClass
    }

    private fun createEnumEntries(kirClass: KirClass, descriptor: ClassDescriptor) {
        if (kirClass.kind != KirClass.Kind.Enum) {
            return
        }

        descriptor.enumEntries.forEachIndexed { index, classDescriptor ->
            KirEnumEntry(
                descriptor = classDescriptor.original,
                owner = kirClass,
                index = index,
            )
        }
    }

    private fun createTypeParameters(kirClass: KirClass, descriptor: ClassDescriptor) {
        if (kirClass.kind != KirClass.Kind.Class) {
            return
        }

        descriptor.typeConstructor.parameters.forEach { typeParameter ->
            KirTypeParameter(
                descriptor = typeParameter,
                parent = kirClass,
            )
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
        val module = kirProvider.getModule(descriptorProvider.getFileModule(file))

        KirClass(
            descriptor = KirClass.Descriptor.File(file),
            name = namer.getFileClassName(file),
            parent = module,
            kind = KirClass.Kind.File,
            superTypes = listOf(baseType),
            isSealed = false,
            hasUnexposedSealedSubclasses = false,
            belongsToSkieKotlinRuntime = module.isSkieKotlinRuntime,
            configuration = sourceFileConfiguration,
        )
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

    private val ClassDescriptor.isExposed: Boolean
        get() = this in descriptorProvider.exposedClasses
}
