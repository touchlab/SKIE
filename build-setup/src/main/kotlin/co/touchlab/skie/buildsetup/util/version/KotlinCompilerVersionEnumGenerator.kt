package co.touchlab.skie.buildsetup.util.version

import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.generateKotlinCode
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

object KotlinCompilerVersionEnumGenerator {

    fun generate(
        kotlinSourceSet: KotlinSourceSet,
        packageName: String,
        makeEnumPublic: Boolean,
        activeVersion: SupportedKotlinVersion?,
    ) {
        val kotlinVersionsEnum = getKotlinCompilerVersionEnumCode(
            packageName = packageName,
            makeEnumPublic = makeEnumPublic,
            activeVersion = activeVersion?.name,
            supportedVersions = SupportedKotlinVersionProvider.getSupportedKotlinVersions(kotlinSourceSet.project),
        )

        kotlinSourceSet.generateKotlinCode("KotlinCompilerVersion.kt", kotlinVersionsEnum)
    }

    private fun getKotlinCompilerVersionEnumCode(
        packageName: String,
        makeEnumPublic: Boolean,
        activeVersion: KotlinToolingVersion?,
        supportedVersions: List<SupportedKotlinVersion>,
    ): String =
        StringBuilder().apply {
            appendLine(
                """
                    |package $packageName
                    |
                    |${if (!makeEnumPublic) "internal " else ""}enum class KotlinCompilerVersion(
                    |   val versionName: String,
                    |   val compilerVersion: String,
                    |   val otherSupportedVersions: List<String>,
                    |   val isEnabled: Boolean,
                    |) {
                    |
                """.trimMargin(),
            )

            supportedVersions.forEach { version ->
                val name = version.name.toString().enquoted()
                val compilerVersion = version.compilerVersion.toString().enquoted()
                val otherSupportedVersions = "listOf(" + version.otherSupportedVersions.joinToString { it.toString().enquoted() } + ")"
                val isEnabled = version.isEnabled

                appendLine(
                    "    ${version.name.toIdentifier()}($name, $compilerVersion, $otherSupportedVersions, $isEnabled),",
                )
            }

            appendLine("    ;")

            appendLine(
                """
                    |
                    |    val supportedVersions: List<String> =
                    |        listOf(compilerVersion) + otherSupportedVersions
                """.trimMargin(),
            )

            if (activeVersion != null) {
                appendLine(
                    """
                    |
                    |    companion object {
                    |
                    |        val current: KotlinCompilerVersion = KotlinCompilerVersion.${activeVersion.toIdentifier()}
                    |    }
                """.trimMargin(),
                )
            }

            appendLine(
                """
                    |}
                """.trimMargin(),
            )
        }.toString()
}
