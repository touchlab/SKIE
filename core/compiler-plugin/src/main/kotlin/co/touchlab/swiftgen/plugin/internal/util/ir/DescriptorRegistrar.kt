package co.touchlab.swiftgen.plugin.internal.util.ir

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.CompositePackageFragmentProviderReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.ModuleDescriptorImplReflector
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderImpl
import org.jetbrains.kotlin.descriptors.impl.MutablePackageFragmentDescriptor
import org.jetbrains.kotlin.name.FqName

internal class DescriptorRegistrar(val moduleDescriptor: ModuleDescriptor) {

    private val basePackage = "co.touchlab.swiftgen.generated"

    private var isFrozen = false

    private val packageBuildersByName = mutableMapOf<String, PackageBuilder>()

    val packages: Collection<PackageBuilder>
        get() = packageBuildersByName.values

    init {
        require(moduleDescriptor.name.asString() == "<Kotlin>") {
            "Descriptors can only be injected into the <Kotlin> module used in the linking pass."
        }
    }

    fun registerDescriptors() {
        freeze()

        val syntheticPackageProvider = createSyntheticPackageProvider()

        registerPackageProvider(syntheticPackageProvider)
    }

    private fun createSyntheticPackageProvider(): PackageFragmentProviderImpl {
        val syntheticPackagedDescriptors = createDummyPackageDescriptors() + packages.map { it.buildPackageDescriptor() }

        return PackageFragmentProviderImpl(syntheticPackagedDescriptors)
    }

    private fun createDummyPackageDescriptors(): List<PackageFragmentDescriptor> =
        basePackage.split(".").scan(emptyList(), List<String>::plus)
            .filter { it.isNotEmpty() }
            .map { it.joinToString(".") }
            .map { MutablePackageFragmentDescriptor(moduleDescriptor, FqName(it)) }

    private fun registerPackageProvider(packageFragmentProvider: PackageFragmentProvider) {
        val existingPackageProvider = moduleDescriptor.reflectedBy<ModuleDescriptorImplReflector>().packageFragmentProviderForModuleContent
        val innerProviders = existingPackageProvider.reflectedBy<CompositePackageFragmentProviderReflector>().providers

        innerProviders.add(packageFragmentProvider)
    }

    fun <D : DeclarationDescriptor> add(fileName: String, declarationBuilder: DeclarationBuilder<D, *, *>): D {
        checkNotFrozen()

        return getOrCreatePackage(fileName).add(declarationBuilder)
    }

    private fun getOrCreatePackage(fileName: String): PackageBuilder =
        packageBuildersByName.getOrPut(fileName) {
            PackageBuilder("$basePackage.$fileName", fileName, moduleDescriptor)
        }

    private fun freeze() {
        isFrozen = true
    }

    private fun checkNotFrozen() {
        check(!isFrozen) { "Cannot add additional IR after the IR generation began." }
    }
}