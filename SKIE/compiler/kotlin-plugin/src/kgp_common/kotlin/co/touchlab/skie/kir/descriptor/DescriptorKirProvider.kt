package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.configuration.FileConfiguration
import co.touchlab.skie.configuration.FileOrClassConfiguration
import co.touchlab.skie.configuration.ModuleConfiguration
import co.touchlab.skie.configuration.PackageConfiguration
import co.touchlab.skie.configuration.RootConfiguration
import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.KirProviderDelegate
import co.touchlab.skie.kir.builtin.DescriptorBasedKirBuiltins
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirElement
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.phases.kir.CreateExposedKirTypesPhase
import co.touchlab.skie.phases.runtime.belongsToSkieKotlinRuntime
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class DescriptorKirProvider(
    private val mainModuleDescriptor: ModuleDescriptor,
    private val kirProvider: KirProvider,
    kotlinBuiltIns: KotlinBuiltIns,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    namer: ObjCExportNamer,
    private val descriptorConfigurationProvider: DescriptorConfigurationProvider,
    rootConfiguration: RootConfiguration,
) : KirProviderDelegate {

    private val kotlinModulesMap = mutableMapOf<String, KirModule>()

    private val kirElementToDescriptorCache = mutableMapOf<KirElement, DeclarationDescriptor>()

    private val classDescriptorToKirCache = mutableMapOf<ClassDescriptor, KirClass>()
    private val kirToClassDescriptorCache = mutableMapOf<KirClass, ClassDescriptor>()

    private val sourceFileToKirCache = mutableMapOf<SourceFile, KirClass>()
    private val kirToSourceFileCache = mutableMapOf<KirClass, SourceFile>()

    private val callableDeclarationDescriptorToKirCache = mutableMapOf<CallableMemberDescriptor, KirCallableDeclaration<*>>()
    private val kirToCallableDeclarationDescriptorCache = mutableMapOf<KirCallableDeclaration<*>, CallableMemberDescriptor>()

    private val typeParameterToDescriptorsCache = mutableMapOf<KirTypeParameter, TypeParameterDescriptor>()

    private val kirToValueParameterDescriptorsCache = mutableMapOf<ParameterDescriptor, KirValueParameter>()

    override val allExternalClassesAndProtocols: Collection<KirClass>
        get() = externalClassesAndProtocolsCache.values

    private val externalClassesAndProtocolsCache = mutableMapOf<ClassDescriptor, KirClass>()

    override val kotlinModules: Collection<KirModule>
        get() = kotlinModulesMap.values

    private val externalModule: KirModule = KirModule(
        name = "<external>",
        project = kirProvider.project,
        configuration = ModuleConfiguration(rootConfiguration),
        origin = KirModule.Origin.External,
    )

    override val stdlibModule: KirModule = getKotlinModule(kotlinBuiltIns.string.module)

    private val externalClassConfiguration = ClassConfiguration(
        FileOrClassConfiguration.File(FileConfiguration(PackageConfiguration(externalModule.configuration))),
    )

    override val kirBuiltins: DescriptorBasedKirBuiltins = DescriptorBasedKirBuiltins(
        stdlibModule = this.stdlibModule,
        kotlinBuiltIns = kotlinBuiltIns,
        extraDescriptorBuiltins = extraDescriptorBuiltins,
        namer = namer,
        descriptorKirProvider = this,
        descriptorConfigurationProvider = descriptorConfigurationProvider,
    )

    fun getKotlinModule(moduleDescriptor: ModuleDescriptor): KirModule {
        val name = (moduleDescriptor.stableName ?: moduleDescriptor.name).asStringStripSpecialMarkers()

        return kotlinModulesMap.getOrPut(name) {
            KirModule(
                name = name,
                project = kirProvider.project,
                configuration = descriptorConfigurationProvider.getConfiguration(moduleDescriptor),
                origin = when {
                    moduleDescriptor.belongsToSkieKotlinRuntime -> KirModule.Origin.SkieRuntime
                    moduleDescriptor == mainModuleDescriptor -> KirModule.Origin.SkieGenerated
                    else -> KirModule.Origin.Kotlin
                },
            )
        }
    }

    fun registerClass(kirClass: KirClass, classDescriptor: ClassDescriptor) {
        kirElementToDescriptorCache[kirClass] = classDescriptor.original

        classDescriptorToKirCache[classDescriptor.original] = kirClass
        kirToClassDescriptorCache[kirClass] = classDescriptor.original
    }

    fun registerFile(kirClass: KirClass, sourceFile: SourceFile) {
        sourceFileToKirCache[sourceFile] = kirClass
        kirToSourceFileCache[kirClass] = sourceFile
    }

    fun registerCallableDeclaration(callableDeclaration: KirCallableDeclaration<*>, callableMemberDescriptor: CallableMemberDescriptor) {
        kirElementToDescriptorCache[callableDeclaration] = callableMemberDescriptor.original

        callableDeclarationDescriptorToKirCache[callableMemberDescriptor.original] = callableDeclaration
        kirToCallableDeclarationDescriptorCache[callableDeclaration] = callableMemberDescriptor.original
    }

    fun registerTypeParameter(typeParameter: KirTypeParameter, typeParameterDescriptor: TypeParameterDescriptor) {
        kirElementToDescriptorCache[typeParameter] = typeParameterDescriptor.original

        typeParameterToDescriptorsCache[typeParameter] = typeParameterDescriptor.original
    }

    fun registerValueParameter(valueParameter: KirValueParameter, parameterDescriptor: ParameterDescriptor) {
        kirElementToDescriptorCache[valueParameter] = parameterDescriptor.original

        kirToValueParameterDescriptorsCache[parameterDescriptor.original] = valueParameter
    }

    fun registerEnumEntry(kirEnumEntry: KirEnumEntry, classDescriptor: ClassDescriptor) {
        kirElementToDescriptorCache[kirEnumEntry] = classDescriptor.original
    }

    fun getClass(classDescriptor: ClassDescriptor): KirClass =
        findClass(classDescriptor)
            ?: error("Class not found: $classDescriptor. This error usually means that the class is not exposed to Objective-C.")

    fun getClass(sourceFile: SourceFile): KirClass =
        findClass(sourceFile)
            ?: error("Class not found: $sourceFile. This error usually means that the class is not exposed to Objective-C.")

    fun getCallableDeclarationDescriptor(callableDeclaration: KirCallableDeclaration<*>): CallableMemberDescriptor =
        kirToCallableDeclarationDescriptorCache[callableDeclaration] ?: error(
            "Callable declaration not found: $callableDeclaration. " +
                "Descriptors must be registered for all instantiated CallableDeclarations.",
        )

    fun getFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction =
        findFunction(functionDescriptor)
            ?: error("Function not found: $functionDescriptor. This error usually means that the function is not exposed to Objective-C.")

    fun getConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor =
        findConstructor(constructorDescriptor)
            ?: error("Constructor not found: $constructorDescriptor. This error usually means that the constructor is not exposed to Objective-C.")

    fun getValueParameter(valueParameterDescriptor: ParameterDescriptor): KirValueParameter =
        findValueParameter(valueParameterDescriptor)
            ?: error("Value parameter not found: $valueParameterDescriptor. Descriptors must be registered for all exported instantiated ParameterDescriptor.")

    fun getTypeParameterDescriptor(kirTypeParameter: KirTypeParameter): TypeParameterDescriptor =
        typeParameterToDescriptorsCache[kirTypeParameter] ?: error(
            "Type parameter not found: $kirTypeParameter. " +
                "Descriptors must be registered for all instantiated TypeParameters.",
        )

    fun getClassDescriptor(kirClass: KirClass): ClassDescriptor =
        findClassDescriptor(kirClass)
            ?: error("Class descriptor not found: $kirClass. Descriptors must be registered for all exported instantiated KirClass.")

    fun getClassSourceFile(kirClass: KirClass): SourceFile =
        findSourceFile(kirClass)
            ?: error("Source file not found: $kirClass. Source files must be registered for all exported instantiated KirClass.")

    fun findClassDescriptor(kirClass: KirClass): ClassDescriptor? =
        kirToClassDescriptorCache[kirClass]

    fun findSourceFile(kirClass: KirClass): SourceFile? =
        kirToSourceFileCache[kirClass]

    fun findClass(classDescriptor: ClassDescriptor): KirClass? =
        classDescriptorToKirCache[classDescriptor.original]

    fun findClass(sourceFile: SourceFile): KirClass? =
        sourceFileToKirCache[sourceFile]

    fun findFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction? =
        callableDeclarationDescriptorToKirCache[functionDescriptor] as? KirSimpleFunction

    fun findConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor? =
        callableDeclarationDescriptorToKirCache[constructorDescriptor] as? KirConstructor

    fun findValueParameter(parameterDescriptor: ParameterDescriptor): KirValueParameter? =
        kirToValueParameterDescriptorsCache[parameterDescriptor]

    fun findDeclarationDescriptor(kirElement: KirElement): DeclarationDescriptor? =
        kirElementToDescriptorCache[kirElement]

    fun getExternalClass(descriptor: ClassDescriptor): KirClass =
        getExternalClass(descriptor, addNSObjectSuperType = true)

    fun getExternalBuiltinClass(descriptor: ClassDescriptor): KirClass =
        getExternalClass(descriptor, addNSObjectSuperType = false)

    private fun getExternalClass(descriptor: ClassDescriptor, addNSObjectSuperType: Boolean): KirClass =
        externalClassesAndProtocolsCache.getOrPut(descriptor.original) {
            val (name, kind) = if (descriptor.kind.isInterface) {
                descriptor.name.asString().removeSuffix("Protocol") to KirClass.Kind.Interface
            } else {
                descriptor.name.asString() to KirClass.Kind.Class
            }

            val kirClass = KirClass(
                kotlinFqName = descriptor.fqNameSafe.asString(),
                objCName = name,
                swiftName = name,
                parent = externalModule,
                kind = kind,
                origin = when {
                    descriptor.fqNameSafe.pathSegments()[0].asString() == "platform" -> KirClass.Origin.PlatformType
                    else -> KirClass.Origin.ExternalCinteropType
                },
                superTypes = if (addNSObjectSuperType && kind != KirClass.Kind.Interface) {
                    listOf(kirBuiltins.NSObject.defaultType)
                } else {
                    emptyList()
                },
                isSealed = false,
                hasUnexposedSealedSubclasses = false,
                nestingLevel = CreateExposedKirTypesPhase.getNestingLevel(descriptor),
                configuration = externalClassConfiguration,
            )

            registerClass(kirClass, descriptor)

            // Obj-C classes do not have type parameters in Kotlin

            kirClass
        }
}
