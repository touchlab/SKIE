package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationTemplate
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.FunctionBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.Namespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.SecondaryConstructorBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.DeserializedClassNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.DeserializedPackageNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace.NewFileNamespace
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template.FunctionTemplate
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.template.SecondaryConstructorTemplate
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectedBy
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
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
    context: CommonBackendContext,
    private val descriptorProvider: DescriptorProvider,
) : DeclarationBuilder {

    private val symbolTable = context.reflectedBy<ContextReflector>().symbolTable

    private lateinit var mainIrModuleFragment: IrModuleFragment

    private val newFileNamespaceFactory = NewFileNamespace.Factory(context, lazy { mainIrModuleFragment }, descriptorProvider)

    private val newFileNamespacesByName = mutableMapOf<String, NewFileNamespace>()
    private val classNamespacesByDescriptor = mutableMapOf<ClassDescriptor, DeserializedClassNamespace>()
    private val originalPackageNamespacesByFile = mutableMapOf<SourceFile, DeserializedPackageNamespace>()
    private val newPackageNamespacesByFile = mutableMapOf<SourceFile, NewFileNamespace>()

    private val allNamespaces: List<Namespace<*>>
        get() = listOf(
            newFileNamespacesByName,
            classNamespacesByDescriptor,
            originalPackageNamespacesByFile,
            newPackageNamespacesByFile,
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

            DeserializedClassNamespace(classDescriptor, descriptorProvider)
        }

    override fun getPackageNamespace(existingMember: FunctionDescriptor): Namespace<PackageFragmentDescriptor> {
        val sourceFile = existingMember.findSourceFileOrNull()

        val hasOriginalPackage = existingMember.findPackage() is DeserializedPackageFragment &&
            sourceFile in descriptorProvider.exposedFiles &&
            sourceFile !in newPackageNamespacesByFile

        return when {
            sourceFile == null -> getCustomNamespace(existingMember.findPackage().name.asStringStripSpecialMarkers())
            hasOriginalPackage -> originalPackageNamespacesByFile.getOrPut(sourceFile) {
                DeserializedPackageNamespace(existingMember, descriptorProvider)
            }
            else -> newPackageNamespacesByFile.getOrPut(sourceFile) {
                newFileNamespaceFactory.create(sourceFile)
            }
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

        namespace.addTemplate(declarationTemplate, symbolTable)

        return declarationTemplate.descriptor
    }

    fun generateIr(mairIrModuleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        this.mainIrModuleFragment = mairIrModuleFragment

        fixPrivateTypeParametersSymbolsFromOldKLibs()

        allNamespaces.forEach {
            it.generateIrDeclarations(pluginContext, symbolTable)
        }

        allNamespaces.forEach {
            it.generateIrBodies(pluginContext)
        }
    }

    private fun fixPrivateTypeParametersSymbolsFromOldKLibs() {
        symbolTable.allExposedTypeParameters(descriptorProvider)
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
