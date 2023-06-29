package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationTemplate
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.FunctionBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.SecondaryConstructorBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.DeserializedClassNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.DeserializedPackageNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.NewFileNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.nameOrError
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template.FunctionTemplate
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template.SecondaryConstructorTemplate
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterPublicSymbolImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeclarationBuilderImpl(
    moduleDescriptor: ModuleDescriptor,
    private val mutableDescriptorProvider: MutableDescriptorProvider,
) : DeclarationBuilder {

    private lateinit var mainIrModuleFragment: IrModuleFragment

    private val newFileNamespaceFactory = NewFileNamespace.Factory(moduleDescriptor, lazy { mainIrModuleFragment })

    private val newFileNamespacesByName = mutableMapOf<String, NewFileNamespace>()
    private val classNamespacesByDescriptor = mutableMapOf<ClassDescriptor, DeserializedClassNamespace>()
    private val originalPackageNamespacesByFile = mutableMapOf<SourceFile, DeserializedPackageNamespace>()

    private var originalExposedFiles: Set<SourceFile> = mutableDescriptorProvider.exposedFiles.toSet()

    init {
        mutableDescriptorProvider.onMutated {
            originalExposedFiles = mutableDescriptorProvider.exposedFiles.toSet()
        }
    }

    private val allNamespaces: List<Namespace<*>>
        get() = listOf(
            newFileNamespacesByName,
            classNamespacesByDescriptor,
            originalPackageNamespacesByFile,
        ).flatMap { it.values }

    override fun getCustomNamespace(name: String): Namespace<PackageFragmentDescriptor> =
        newFileNamespacesByName.getOrPut(name) {
            newFileNamespaceFactory.create(name)
        }

    override fun getClassNamespace(classDescriptor: ClassDescriptor): Namespace<ClassDescriptor> =
        classNamespacesByDescriptor.getOrPut(classDescriptor) {
            require(classDescriptor is DeserializedClassDescriptor) {
                "Only DeserializedClassDescriptor is currently supported. Was: $classDescriptor"
            }

            DeserializedClassNamespace(classDescriptor)
        }

    override fun getPackageNamespace(existingMember: FunctionDescriptor): Namespace<PackageFragmentDescriptor> {
        val sourceFile = existingMember.findSourceFileOrNull()

        val hasOriginalPackage = existingMember.findPackage() is DeserializedPackageFragment &&
            sourceFile in originalExposedFiles

        return when {
            sourceFile == null -> getCustomNamespace(existingMember.findPackage().name.asStringStripSpecialMarkers())
            hasOriginalPackage -> originalPackageNamespacesByFile.getOrPut(sourceFile) {
                DeserializedPackageNamespace(existingMember)
            }
            else -> getCustomNamespace(sourceFile.nameOrError)
        }
    }

    private fun CallableMemberDescriptor.findSourceFileOrNull(): SourceFile? = try {
        this.findSourceFile()
    } catch (_: Throwable) {
        null
    }

    override fun createFunction(
        name: Name,
        namespace: Namespace<*>,
        annotations: Annotations,
        builder: FunctionBuilder.() -> Unit,
    ): FunctionDescriptor =
        create(namespace) { FunctionTemplate(name, namespace, annotations, builder) }

    override fun createSecondaryConstructor(
        name: Name,
        namespace: Namespace<ClassDescriptor>,
        annotations: Annotations,
        builder: SecondaryConstructorBuilder.() -> Unit,
    ): ClassConstructorDescriptor =
        create(namespace) { SecondaryConstructorTemplate(name, namespace, annotations, builder) }

    private fun <D : DeclarationDescriptor> create(
        namespace: Namespace<*>,
        templateBuilder: () -> DeclarationTemplate<D>,
    ): D {
        val declarationTemplate = templateBuilder()

        mutableDescriptorProvider.mutate {
            namespace.addTemplate(declarationTemplate)
        }

        return declarationTemplate.descriptor
    }

    fun declareSymbols(symbolTable: SymbolTable) {
        allNamespaces.forEach {
            it.registerSymbols(symbolTable)
        }
    }

    fun generateIr(mairIrModuleFragment: IrModuleFragment, pluginContext: IrPluginContext, symbolTable: SymbolTable) {
        this.mainIrModuleFragment = mairIrModuleFragment

        fixPrivateTypeParametersSymbolsFromOldKLibs(symbolTable)

        allNamespaces.forEach {
            it.generateIrDeclarations(pluginContext, symbolTable)
        }

        allNamespaces.forEach {
            it.generateIrBodies(pluginContext)
        }
    }

    /**
     * This fixes a bug in klibs produced by Konan <= 1.5.
     * (We don't know for sure because we don't know what is different about the klib, but we don't have a different type of reproducer.)
     * The affected klibs behave differently during IR deserialization - exposed type parameters are deserialized without public symbols.
     * This fix registers these missing symbols manually.
     */
    private fun fixPrivateTypeParametersSymbolsFromOldKLibs(symbolTable: SymbolTable) {
        symbolTable.allExposedTypeParameters(mutableDescriptorProvider)
            .filter { it.symbol !is IrTypeParameterPublicSymbolImpl }
            .forEach {
                symbolTable.declarePrivateTypeParameterAsPublic(it)
            }
    }
}

private fun SymbolTable.allExposedTypeParameters(descriptorProvider: DescriptorProvider): List<IrTypeParameter> =
    (descriptorProvider.allExposedMembers.flatMap { referenceBoundTypeParameterContainer(it) } +
        descriptorProvider.exposedClasses.flatMap { referenceBoundTypeParameterContainer(it) })
        .flatMap { it.typeParameters }

private fun SymbolTable.referenceBoundTypeParameterContainer(
    callableMemberDescriptor: CallableMemberDescriptor,
): List<IrTypeParametersContainer> =
    when (callableMemberDescriptor) {
        is FunctionDescriptor -> referenceBoundTypeParameterContainer(callableMemberDescriptor)
        is PropertyDescriptor -> referenceBoundTypeParameterContainer(callableMemberDescriptor)
        else -> error("Unsupported type $callableMemberDescriptor.")
    }

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun SymbolTable.referenceBoundTypeParameterContainer(functionDescriptor: FunctionDescriptor): List<IrTypeParametersContainer> =
    listOfNotNull(
        referenceFunction(functionDescriptor).takeIf { it.isBound }?.owner
    )

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun SymbolTable.referenceBoundTypeParameterContainer(propertyDescriptor: PropertyDescriptor): List<IrTypeParametersContainer> {
    val property = referenceProperty(propertyDescriptor).takeIf { it.isBound }?.owner

    return listOfNotNull(property?.getter, property?.setter)
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun SymbolTable.referenceBoundTypeParameterContainer(classDescriptor: ClassDescriptor): List<IrTypeParametersContainer> =
    listOfNotNull(
        referenceClass(classDescriptor).takeIf { it.isBound }?.owner
    )

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun SymbolTable.declarePrivateTypeParameterAsPublic(typeParameter: IrTypeParameter) {
    val signature = signaturer.composeSignature(typeParameter.descriptor) ?: error("Type parameter $typeParameter is not exposed.")

    val publicSymbol = IrTypeParameterPublicSymbolImpl(signature, typeParameter.descriptor)
    publicSymbol.bind(typeParameter)

    declareGlobalTypeParameter(signature, { publicSymbol }, { typeParameter })
}
