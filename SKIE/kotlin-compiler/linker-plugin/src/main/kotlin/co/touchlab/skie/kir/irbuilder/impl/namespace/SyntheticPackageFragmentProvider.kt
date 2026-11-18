package co.touchlab.skie.kir.irbuilder.impl.namespace

import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderOptimized
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// Reads `packageFragments` on every call, because SKIE adds fragments after the provider is registered.
internal class SyntheticPackageFragmentProvider(
    private val packageFragments: Collection<PackageFragmentDescriptor>,
) : PackageFragmentProviderOptimized {

    override fun collectPackageFragments(fqName: FqName, packageFragments: MutableCollection<PackageFragmentDescriptor>) {
        this.packageFragments.filterTo(packageFragments) { it.fqName == fqName }
    }

    override fun isEmpty(fqName: FqName): Boolean =
        packageFragments.none { it.fqName == fqName }

    @Deprecated("for usages use #packageFragments(FqName) at final point, for impl use #collectPackageFragments(FqName, MutableCollection<PackageFragmentDescriptor>)")
    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> =
        packageFragments.filter { it.fqName == fqName }

    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> =
        packageFragments
            .map { it.fqName }
            .filter { !it.isRoot && it.parent() == fqName }
            .distinct()
}
