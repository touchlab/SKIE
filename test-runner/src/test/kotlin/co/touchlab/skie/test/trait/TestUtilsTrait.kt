package co.touchlab.skie.test.trait

import co.touchlab.skie.test.trait.gradle.BuildGradleBuilder
import co.touchlab.skie.test.util.StringBuilderScope
import co.touchlab.skie.test.util.buildString
import java.io.File

interface TestUtilsTrait {
    val workingDirectory: File
        get() = File(System.getProperty("user.dir"))
    val tempRepository: File
        get() = File(System.getProperty("smokeTestRepository"))

    operator fun File.invoke(content: String) = writeText(content)

    operator fun File.invoke(content: BuildGradleBuilder) = writeText(content.toString())


    operator fun File.invoke(contentBuilder: StringBuilderScope.() -> Unit) = writeText(buildString(contentBuilder))

    operator fun File.invoke(): String = readText()
}
