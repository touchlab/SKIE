package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.namespace

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ModuleDescriptorImplReflector
import co.touchlab.swiftlink.plugin.moduleDescriptorOrNull
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

internal class NewFileNamespace private constructor(
    name: FqName,
    private val context: Context,
) : BaseNamespace<PackageFragmentDescriptor>() {

    private val packageContent = mutableListOf<DeclarationDescriptor>()

    override val descriptor: PackageFragmentDescriptor = object : PackageFragmentDescriptorImpl(
        module = context.moduleDescriptor,
        fqName = name,
    ) {

        private val memberScope = SimpleMemberScope(packageContent)

        override fun getMemberScope(): MemberScope = memberScope
    }

    private val fileName = name.asString().split(".").joinToString("/") + ".kt"

    override val sourceElement: SourceElement = SourceElement { SourceFile { fileName } }

    override fun addDescriptor(declarationDescriptor: DeclarationDescriptor) {
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
            name, 0, 0, 0, -1, 0, 0,
        )
    }

    class Factory(context: CommonBackendContext, mainIrModuleFragment: Lazy<IrModuleFragment>) {

        private val moduleDescriptor = requireNotNull(context.moduleDescriptorOrNull) { "Context must have a module descriptor." }

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

            val namespace = NewFileNamespace(fqName, namespaceContext)

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
            fqName.asString().split(".").scan(emptyList(), List<String>::plus)
                .filter { it.isNotEmpty() }
                .map { it.joinToString(".") }
                .map { MutablePackageFragmentDescriptor(moduleDescriptor, FqName(it)) }
    }

    private class Context(val moduleDescriptor: ModuleDescriptor, mainIrModuleFragment: Lazy<IrModuleFragment>) {

        val mainIrModuleFragment: IrModuleFragment by mainIrModuleFragment

        val files: MutableList<IrFile>
            get() = mainIrModuleFragment.files
    }

    companion object {

        private const val basePackage: String = "co.touchlab.swiftgen.generated"
    }
}
