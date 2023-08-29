@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.skie.plugin.reflection.reflectors.ModuleDescriptorImplReflector
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
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.source.PsiSourceFile

internal class NewFileNamespace private constructor(
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

    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer {
        val fileEntry = DummyIrFileEntry(sourceFile.nameOrError)

        val file = IrFileImpl(fileEntry, descriptor, context.mainIrModuleFragment)

        context.files.add(file)

        return file
    }

    private class DummyIrFileEntry(override val name: String) : IrFileEntry {

        override val maxOffset: Int = 0

        override fun getColumnNumber(offset: Int): Int = 0

        override fun getLineNumber(offset: Int): Int = 0

        override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo = SourceRangeInfo(
            name, 0, 0, 0, 0, 0, 0,
        )
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
