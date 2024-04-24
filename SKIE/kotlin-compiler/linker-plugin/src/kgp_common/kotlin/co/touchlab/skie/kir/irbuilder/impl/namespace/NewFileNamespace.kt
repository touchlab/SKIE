package co.touchlab.skie.kir.irbuilder.impl.namespace

import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.skie.compilerinject.reflection.reflectors.ModuleDescriptorImplReflector
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.shim.createDummyIrFileEntry
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.common.SimpleMemberScope
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderImpl
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.impl.MutablePackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.source.PsiSourceFile

class NewFileNamespace private constructor(
    private val sourceFile: SourceFile,
    private val context: Context,
) : BaseNamespace<PackageFragmentDescriptor>() {

    private constructor(
        fileName: String,
        context: Context,
    ) : this(SourceFile { fileName }, context)

    private val packageContent = mutableListOf<DeclarationDescriptor>()

    override val descriptor: PackageFragmentDescriptor = object : PackageFragmentDescriptorImpl(
        module = context.moduleDescriptor,
        fqName = FqName("$basePackage.${sourceFile.nameOrError.removeSuffix(".kt")}"),
    ) {

        private val memberScope = SimpleMemberScope(packageContent)

        override fun getMemberScope(): MemberScope = memberScope
    }

    override val sourceElement: SourceElement = SourceElement { sourceFile }

    override fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor) {
        packageContent.add(declarationDescriptor)
    }

    context(KotlinIrPhase.Context)
    override fun generateNamespaceIr(): IrDeclarationContainer {
        val fileEntry = createDummyIrFileEntry(sourceFile.nameOrError)

        val file = IrFileImpl(fileEntry, descriptor, context.mainIrModuleFragment)

        context.files.add(file)

        return file
    }

    class Factory(
        private val moduleDescriptor: ModuleDescriptor,
        mainIrModuleFragment: Lazy<IrModuleFragment>,
    ) {

        private val namespaceContext = Context(moduleDescriptor, mainIrModuleFragment)

        private val packagesByName = mutableMapOf<FqName, PackageFragmentDescriptor>()

        init {
            val syntheticPackageProvider = PackageFragmentProviderImpl(packagesByName.values)

            val moduleDescriptor = moduleDescriptor.reflectedBy<ModuleDescriptorImplReflector>()
            val packageProvider = moduleDescriptor.packageFragmentProviderForModuleContent
            val compositePackageProvider = packageProvider.reflectedBy<CompositePackageFragmentProviderReflector>()
            val composedProviders = compositePackageProvider.providers

            // TODO: Do we need the synthetic package provider with the new compiler phases?
            composedProviders.add(syntheticPackageProvider)
        }

        fun create(name: String): NewFileNamespace {
            val namespace = NewFileNamespace(name, namespaceContext)

            addPackageDescriptor(namespace.descriptor)

            return namespace
        }

        private fun addPackageDescriptor(packageDescriptor: PackageFragmentDescriptor) {
            createDummyPackageDescriptors(packageDescriptor.fqName).forEach {
                packagesByName.putIfAbsent(it.fqName, it)
            }

            packagesByName[packageDescriptor.fqName] = packageDescriptor
        }

        private fun createDummyPackageDescriptors(fqName: FqName): List<PackageFragmentDescriptor> =
            fqName.asString()
                .split(".")
                .asSequence()
                .map { it.toValidSwiftIdentifier() }
                .scan(emptyList(), List<String>::plus)
                .filter { it.isNotEmpty() }
                .map { it.joinToString(".") }
                .map { MutablePackageFragmentDescriptor(moduleDescriptor, FqName(it)) }
                .toList()
    }

    private class Context(val moduleDescriptor: ModuleDescriptor, mainIrModuleFragment: Lazy<IrModuleFragment>) {

        val mainIrModuleFragment: IrModuleFragment by mainIrModuleFragment

        val files: MutableList<IrFile>
            get() = mainIrModuleFragment.files
    }

    companion object {

        private const val basePackage: String = "co.touchlab.skie.generated"
    }
}

// From ObjCExportNamer.getFileClassName - ensures that this code crashes in the same cases.
val SourceFile.nameOrError: String
    get() = when (this) {
        is PsiSourceFile -> {
            val psiFile = psiFile
            val ktFile = psiFile as? KtFile ?: error("PsiFile '$psiFile' is not KtFile")
            ktFile.name
        }
        else -> name ?: error("$this has no name")
    }
