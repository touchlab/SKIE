package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.Reflector
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider

internal class CompositePackageFragmentProviderReflector(
    override val instance: CompositePackageFragmentProvider,
) : Reflector(CompositePackageFragmentProvider::class) {

    val providers by declaredField<ArrayList<PackageFragmentProvider>>()
}
