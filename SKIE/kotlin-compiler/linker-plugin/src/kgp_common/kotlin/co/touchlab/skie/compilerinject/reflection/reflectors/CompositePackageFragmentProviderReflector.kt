package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider

class CompositePackageFragmentProviderReflector(override val instance: CompositePackageFragmentProvider) :
    Reflector(CompositePackageFragmentProvider::class) {

    val providers by declaredField<ArrayList<PackageFragmentProvider>>()
}
