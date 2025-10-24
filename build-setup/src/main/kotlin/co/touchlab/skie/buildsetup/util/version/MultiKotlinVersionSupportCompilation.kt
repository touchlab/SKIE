package co.touchlab.skie.buildsetup.util.version

import org.gradle.api.Named
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation

class MultiKotlinVersionSupportCompilation(
    val supportedKotlinVersion: SupportedKotlinVersion,
    val kotlinCompilation: KotlinWithJavaCompilation<*, KotlinJvmCompilerOptions>,
) : Named {

    override fun getName(): String = supportedKotlinVersion.name.toString()
}
