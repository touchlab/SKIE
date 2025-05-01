package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.configuration.provider.descriptor.DescriptorConfigurationProvider
import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.KirProviderDelegate
import co.touchlab.skie.kir.builtin.DescriptorBasedKirBuiltins
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirElement
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirTypeParameter
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.util.belongsToSkieKotlinRuntime
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
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class DescriptorKirProvider(
    private val mainModuleDescriptor: ModuleDescriptor,
    private val kirProvider: KirProvider,
    kotlinBuiltIns: KotlinBuiltIns,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    namer: ObjCExportNamer,
    private val descriptorConfigurationProvider: DescriptorConfigurationProvider,
    globalConfiguration: GlobalConfiguration,
) : KirProviderDelegate {

    private val externalDescriptorKirProvider = ExternalDescriptorKirProvider(
        kirProject = kirProvider.project,
        globalConfiguration = globalConfiguration,
        descriptorConfigurationProvider = descriptorConfigurationProvider,
        descriptorKirProvider = lazy { this },
    )

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
        get() = externalDescriptorKirProvider.allExternalClassesAndProtocols

    override val kotlinModules: Collection<KirModule>
        get() = kotlinModulesMap.values

    override val stdlibModule: KirModule = getKotlinModule(kotlinBuiltIns.string.module)

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

    fun getClass(classDescriptor: ClassDescriptor): KirClass = findClass(classDescriptor)
        ?: error("Class not found: $classDescriptor. This error usually means that the class is not exposed to Objective-C.")

    fun getClass(sourceFile: SourceFile): KirClass = findClass(sourceFile)
        ?: error("Class not found: $sourceFile. This error usually means that the class is not exposed to Objective-C.")

    fun getCallableDeclarationDescriptor(callableDeclaration: KirCallableDeclaration<*>): CallableMemberDescriptor =
        kirToCallableDeclarationDescriptorCache[callableDeclaration] ?: error(
            "Callable declaration not found: $callableDeclaration. " +
                "Descriptors must be registered for all instantiated CallableDeclarations.",
        )

    fun getFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction = findFunction(functionDescriptor)
        ?: error("Function not found: $functionDescriptor. This error usually means that the function is not exposed to Objective-C.")

    fun getConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor = findConstructor(constructorDescriptor)
        ?: error(
            "Constructor not found: $constructorDescriptor. This error usually means that the constructor is not exposed to Objective-C.",
        )

    fun getValueParameter(valueParameterDescriptor: ParameterDescriptor): KirValueParameter = findValueParameter(valueParameterDescriptor)
        ?: error(
            "Value parameter not found: $valueParameterDescriptor. Descriptors must be registered for all exported instantiated ParameterDescriptor.",
        )

    fun getTypeParameterDescriptor(kirTypeParameter: KirTypeParameter): TypeParameterDescriptor =
        typeParameterToDescriptorsCache[kirTypeParameter] ?: error(
            "Type parameter not found: $kirTypeParameter. " +
                "Descriptors must be registered for all instantiated TypeParameters.",
        )

    fun getClassDescriptor(kirClass: KirClass): ClassDescriptor = findClassDescriptor(kirClass)
        ?: error("Class descriptor not found: $kirClass. Descriptors must be registered for all exported instantiated KirClass.")

    fun getClassSourceFile(kirClass: KirClass): SourceFile = findSourceFile(kirClass)
        ?: error("Source file not found: $kirClass. Source files must be registered for all exported instantiated KirClass.")

    fun findClassDescriptor(kirClass: KirClass): ClassDescriptor? = kirToClassDescriptorCache[kirClass]

    fun findSourceFile(kirClass: KirClass): SourceFile? = kirToSourceFileCache[kirClass]

    fun findClass(classDescriptor: ClassDescriptor): KirClass? = classDescriptorToKirCache[classDescriptor.original]

    fun findClass(sourceFile: SourceFile): KirClass? = sourceFileToKirCache[sourceFile]

    fun findFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction? =
        callableDeclarationDescriptorToKirCache[functionDescriptor] as? KirSimpleFunction

    fun findConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor? =
        callableDeclarationDescriptorToKirCache[constructorDescriptor] as? KirConstructor

    fun findValueParameter(parameterDescriptor: ParameterDescriptor): KirValueParameter? =
        kirToValueParameterDescriptorsCache[parameterDescriptor]

    fun findDeclarationDescriptor(kirElement: KirElement): DeclarationDescriptor? = kirElementToDescriptorCache[kirElement]

    fun getExternalClass(descriptor: ClassDescriptor): KirClass = externalDescriptorKirProvider.getExternalClass(descriptor)

    fun getExternalBuiltinClass(descriptor: ClassDescriptor): KirClass = externalDescriptorKirProvider.getExternalBuiltinClass(descriptor)
}
