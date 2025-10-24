package co.touchlab.skie.buildsetup.main.extensions

import co.touchlab.skie.buildsetup.util.version.MultiKotlinVersionSupportCompilation
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class MultiKotlinVersionSupportExtension @Inject constructor(objects: ObjectFactory) {

    val sharedApiConfigurationName = "sharedApi"
    val sharedImplementationConfigurationName = "sharedImplementation"
    val sharedCompileOnlyConfigurationName = "sharedCompileOnly"
    val sharedRuntimeOnlyConfigurationName = "sharedRuntimeOnly"

    val compilations: NamedDomainObjectSet<MultiKotlinVersionSupportCompilation> =
        objects.namedDomainObjectSet(MultiKotlinVersionSupportCompilation::class.java)
}
