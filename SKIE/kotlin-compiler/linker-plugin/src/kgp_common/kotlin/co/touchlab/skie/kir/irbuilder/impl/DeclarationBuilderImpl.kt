package co.touchlab.skie.kir.irbuilder.impl

import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.allExposedMembers
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder
import co.touchlab.skie.kir.irbuilder.DeclarationTemplate
import co.touchlab.skie.kir.irbuilder.FunctionBuilder
import co.touchlab.skie.kir.irbuilder.Namespace
import co.touchlab.skie.kir.irbuilder.SecondaryConstructorBuilder
import co.touchlab.skie.kir.irbuilder.impl.namespace.DeserializedClassNamespace
import co.touchlab.skie.kir.irbuilder.impl.namespace.DeserializedPackageNamespace
import co.touchlab.skie.kir.irbuilder.impl.namespace.NewFileNamespace
import co.touchlab.skie.kir.irbuilder.impl.namespace.nameOrError
import co.touchlab.skie.kir.irbuilder.impl.symboltable.DummyIrConstructor
import co.touchlab.skie.kir.irbuilder.impl.symboltable.DummyIrSimpleFunction
import co.touchlab.skie.kir.irbuilder.impl.template.FunctionTemplate
import co.touchlab.skie.kir.irbuilder.impl.template.SecondaryConstructorTemplate
import co.touchlab.skie.kir.util.SkieSymbolTable
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.SymbolTablePhase
import co.touchlab.skie.phases.moduleFragment
import co.touchlab.skie.phases.skieSymbolTable
import co.touchlab.skie.shim.findPackage
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.common.serialization.signature.PublicIdSignatureComputer
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterPublicSymbolImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.DeserializedPackageFragment
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

class DeclarationBuilderImpl(
    moduleDescriptor: ModuleDescriptor,
    private val mutableDescriptorProvider: MutableDescriptorProvider,
) : DeclarationBuilder {

    private lateinit var mainIrModuleFragment: IrModuleFragment

    private val newFileNamespaceFactory = NewFileNamespace.Factory(moduleDescriptor, lazy { mainIrModuleFragment })

    private val newFileNamespacesByName = mutableMapOf<String, NewFileNamespace>()
    private val classNamespacesByDescriptor = mutableMapOf<ClassDescriptor, DeserializedClassNamespace>()
    private val originalPackageNamespacesByFile = mutableMapOf<SourceFile, DeserializedPackageNamespace>()

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
            sourceFile in mutableDescriptorProvider.exposedFiles

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

        with(mutableDescriptorProvider) {
            namespace.addTemplate(declarationTemplate)
        }

        return declarationTemplate.descriptor
    }

    context(SymbolTablePhase.Context)
    fun declareSymbols() {
        allNamespaces.forEach {
            it.registerSymbols()
        }
    }

    context(KotlinIrPhase.Context)
    fun generateIr() {
        this.mainIrModuleFragment = moduleFragment

        fixPrivateTypeParametersSymbolsFromOldKLibs(skieSymbolTable)

        allNamespaces.forEach {
            it.generateIrDeclarations()
        }

        allNamespaces.forEach {
            it.generateIrBodies()
        }
    }

    /**
     * This fixes a bug in klibs produced by Konan <= 1.5.
     * The affected klibs behave differently during IR deserialization - some exposed type parameters are deserialized without public symbols.
     * We do not fully understand the reason behind that but our guess is that older versions of the compiler did not correctly linked TypeParameter declarations with their usage.
     * It looks like that for some TypeParameters the compiler created new instances instead of using the one available in the declaration scope.
     * This then probably confuses the deserializer which in return declares the TypeParameters with non-public symbol (because it does not see that symbol as being declared in public declaration).
     * This fix registers these missing symbols manually.
     */
    private fun fixPrivateTypeParametersSymbolsFromOldKLibs(skieSymbolTable: SkieSymbolTable) {
        skieSymbolTable.allExposedTypeParameters(mutableDescriptorProvider)
            .filter { it.symbol !is IrTypeParameterPublicSymbolImpl }
            .forEach {
                skieSymbolTable.declarePrivateTypeParameterAsPublic(it)
            }
    }
}

private fun SkieSymbolTable.allExposedTypeParameters(descriptorProvider: DescriptorProvider): List<IrTypeParameter> =
    (descriptorProvider.allExposedMembers.flatMap { referenceBoundTypeParameterContainer(it) } +
        descriptorProvider.exposedClasses.flatMap { referenceBoundTypeParameterContainer(it) })
        .flatMap { it.typeParameters }

private fun SkieSymbolTable.referenceBoundTypeParameterContainer(
    callableMemberDescriptor: CallableMemberDescriptor,
): List<IrTypeParametersContainer> =
    when (callableMemberDescriptor) {
        is FunctionDescriptor -> referenceBoundTypeParameterContainer(callableMemberDescriptor)
        is PropertyDescriptor -> referenceBoundTypeParameterContainer(callableMemberDescriptor)
        else -> error("Unsupported type $callableMemberDescriptor.")
    }

private fun SkieSymbolTable.referenceBoundTypeParameterContainer(functionDescriptor: FunctionDescriptor): List<IrTypeParametersContainer> =
    listOfNotNull(
        descriptorExtension.referenceFunction(functionDescriptor).takeIf {
            it.isBound && it.owner !is DummyIrSimpleFunction && it.owner !is DummyIrConstructor
        }?.owner,
    )

private fun SkieSymbolTable.referenceBoundTypeParameterContainer(propertyDescriptor: PropertyDescriptor): List<IrTypeParametersContainer> {
    val property = descriptorExtension.referenceProperty(propertyDescriptor).takeIf { it.isBound }?.owner

    return listOfNotNull(property?.getter, property?.setter)
}

private fun SkieSymbolTable.referenceBoundTypeParameterContainer(classDescriptor: ClassDescriptor): List<IrTypeParametersContainer> =
    listOfNotNull(
        descriptorExtension.referenceClass(classDescriptor).takeIf { it.isBound }?.owner,
    )

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun SkieSymbolTable.declarePrivateTypeParameterAsPublic(typeParameter: IrTypeParameter) {
    val signature = PublicIdSignatureComputer(KonanManglerIr).computeSignature(typeParameter)

    val publicSymbol = IrTypeParameterPublicSymbolImpl(signature, typeParameter.descriptor)
    publicSymbol.bind(typeParameter)

    kotlinSymbolTable.declareGlobalTypeParameter(signature, { publicSymbol }, { typeParameter })
}
