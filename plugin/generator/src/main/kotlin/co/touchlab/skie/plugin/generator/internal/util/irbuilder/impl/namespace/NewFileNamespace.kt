@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectedBy
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ModuleDescriptorImplReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
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
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class NewFileNamespace private constructor(
    name: FqName,
    private val context: Context,
    descriptorProvider: DescriptorProvider,
) : BaseNamespace<PackageFragmentDescriptor>(descriptorProvider) {

    private val packageContent = mutableListOf<DeclarationDescriptor>()

    override val descriptor: PackageFragmentDescriptor = object : PackageFragmentDescriptorImpl(
        module = context.moduleDescriptor,
        fqName = name,
    ) {

        private val memberScope = SimpleMemberScope(packageContent)

        override fun getMemberScope(): MemberScope = memberScope
    }

    private val fileName = name.asString().split(".").joinToString("/") + ".kt"

    private val sourceFile = SourceFile { fileName }

    override val sourceElement: SourceElement = SourceElement { sourceFile }

    override fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor) {
        packageContent.add(declarationDescriptor)
    }

    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer {
        val fileEntry = DummyIrFileEntry(fileName)

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
        context: CommonBackendContext,
        mainIrModuleFragment: Lazy<IrModuleFragment>,
        private val descriptorProvider: DescriptorProvider,
    ) {

        private val moduleDescriptor = requireNotNull((context as? KonanContext)?.moduleDescriptor) { "Context must have a module descriptor." }

        private val namespaceContext = Context(moduleDescriptor, mainIrModuleFragment)

        private val packagesByName = mutableMapOf<FqName, PackageFragmentDescriptor>()

        init {
            val syntheticPackageProvider = PackageFragmentProviderImpl(packagesByName.values)

            val moduleDescriptor = moduleDescriptor.reflectedBy<ModuleDescriptorImplReflector>()
            val packageProvider = moduleDescriptor.packageFragmentProviderForModuleContent
            val compositePackageProvider = packageProvider.reflectedBy<CompositePackageFragmentProviderReflector>()
            val composedProviders = compositePackageProvider.providers

            composedProviders.add(syntheticPackageProvider)
        }

        fun create(name: String): NewFileNamespace {
            val fqName = FqName("$basePackage.$name")

            val namespace = NewFileNamespace(fqName, namespaceContext, descriptorProvider)

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
