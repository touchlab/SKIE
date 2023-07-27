package co.touchlab.skie.buildsetup.plugins.extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension

interface HasSigningPlugin {
    val Project.signing: SigningExtension
        get() = extensions.getByType()

    fun Project.signing(configure: SigningExtension.() -> Unit) = extensions.configure(configure)
}
