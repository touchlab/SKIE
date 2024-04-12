package co.touchlab.skie.test.trait.gradle

import co.touchlab.skie.test.util.KotlinVersion
import java.io.File

class BuildGradleFile(
    val file: File,
) {
    operator fun invoke(kotlinVersion: KotlinVersion, block: BuildGradleBuilder.() -> Unit) {
        val builder = BuildGradleBuilder(kotlinVersion)
        builder.block()
        file.writeText(builder.toString())
    }
}
