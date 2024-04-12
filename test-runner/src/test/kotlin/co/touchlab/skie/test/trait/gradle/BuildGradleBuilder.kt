package co.touchlab.skie.test.trait.gradle

import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import org.intellij.lang.annotations.Language

class BuildGradleBuilder(
    val kotlinVersion: KotlinVersion,
    val plugins: List<Plugin> = listOf(
        Plugin.kotlinMultiplatform(kotlinVersion),
        Plugin.skie,
    ),
) {
    private val imports = mutableSetOf<String>()
    private val builder = StringBuilder()
    private var builderIndentationLevel = 0

    init {
        if (plugins.isNotEmpty()) {
            "plugins" {
                plugins.forEach {
                    +it.application
                }
            }
        }
    }

    fun kotlin(block: KotlinExtensionScope.() -> Unit) {
        "kotlin" {
            KotlinExtensionScope().block()
        }
    }

    fun kotlinArtifacts(block: KotlinArtifactsExtensionScope.() -> Unit) {
        "kotlinArtifacts" {
            KotlinArtifactsExtensionScope().block()
        }
    }

    fun workaroundFatFrameworkConfigurationIfNeeded(
        kotlinVersion: KotlinVersion,
    ) {
        if (kotlinVersion.value.startsWith("1.8.")) {
            imports.add("org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask")

            appendLines("""
                tasks.withType<FatFrameworkTask>().configureEach {
                    configurations.getByName(
                        name.substringAfter("link").replaceFirstChar { it.lowercase() }
                    ).attributes {
                        attribute(Attribute.of("fat-framework", String::class.java), "true")
                    }
                }
            """.trimIndent())
        }
    }

    fun appendLines(@Language("kotlin") code: String) {
        code.lines().forEach {
            +it
        }
    }

    operator fun @receiver:Language("kotlin") String.unaryPlus() {
        appendIndentation()
        builder.appendLine(this)
    }

    operator fun @receiver:Language("kotlin") String.invoke(block: () -> Unit) {
        appendIndentation()
        builder.append(this)
        builder.appendLine(" {")
        builderIndentationLevel += 1
        block()
        builderIndentationLevel -= 1
        builder.appendLine("}")
    }

    private fun appendIndentation() {
        repeat(builderIndentationLevel) {
            builder.append("    ")
        }
    }

    override fun toString(): String {
        return buildString {
            imports.sorted().forEach {
                this.appendLine("import $it")
            }
            if (imports.isNotEmpty()) {
                this.appendLine()
            }
            this.append(builder)
        }
    }

    inner class KotlinExtensionScope {
        fun androidTarget() = target(KotlinTarget.AndroidTarget)

        fun iosArm64() = target(KotlinTarget.Native.Ios.Arm64)

        fun iosX64() = target(KotlinTarget.Native.Ios.X64)

        fun iosSimulatorArm64() = target(KotlinTarget.Native.Ios.SimulatorArm64)

        fun macosArm64() = target(KotlinTarget.Native.MacOS.Arm64)

        fun macosX64() = target(KotlinTarget.Native.MacOS.X64)

        fun allIos() = targets(KotlinTarget.Native.Ios)

        fun allMacos() = targets(KotlinTarget.Native.MacOS)

        fun allTvos() = targets(KotlinTarget.Native.Tvos)

        fun allDarwin() {
            allIos()
            allMacos()
            allTvos()
            // TODO: allTvos()
            // TODO: allWatchos()
        }

        fun all() {
            androidTarget()
            allDarwin()

            // TODO: Add the rest
        }

        fun targets(preset: KotlinTarget.Preset) = preset.targets.forEach {
            target(it)
        }

        fun target(target: KotlinTarget) = +"${target.id}()"

        fun registerNativeFrameworks(
            kotlinVersion: KotlinVersion,
            buildConfiguration: BuildConfiguration,
            linkMode: LinkMode,
        ) {
            imports.add("org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget")
            imports.add("org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType")

            "targets.withType<KotlinNativeTarget>" {
                "binaries" {
                    "framework(buildTypes = listOf(NativeBuildType.${buildConfiguration.name.uppercase()}))" {
                        +"isStatic = ${linkMode.isStatic}"
                        +"""freeCompilerArgs = freeCompilerArgs + listOf("-Xbinary=bundleId=gradle_test")"""
                        if (kotlinVersion.value.startsWith("1.8.") || kotlinVersion.value.startsWith("1.9.0")) {
                            +"""linkerOpts += "-ld64""""
                        }
                    }
                }
            }
        }
    }

    inner class KotlinArtifactsExtensionScope {

        fun framework(
            kotlinVersion: KotlinVersion,
            target: KotlinTarget.Native,
            linkMode: LinkMode,
            buildConfiguration: BuildConfiguration,
        ) {
            imports.add("org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType")

            "Native.Framework" {
                +"target = ${target.id}"
                +"isStatic = ${linkMode.isStatic}"
                +"modes(NativeBuildType.${buildConfiguration.name.uppercase()})"
                "toolOptions" {
                    +"""freeCompilerArgs.add("-Xbinary=bundleId=gradle_test")"""
                }
                if (kotlinVersion.value.startsWith("1.8.") || kotlinVersion.value.startsWith("1.9.0")) {
                    +"""linkerOptions += "-ld64""""
                }
            }
        }

        fun xcframework(
            kotlinVersion: KotlinVersion,
            targets: List<KotlinTarget.Native>,
            linkMode: LinkMode,
            buildConfiguration: BuildConfiguration,
        ) {
            imports.add("org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType")

            "Native.XCFramework" {
                +"targets(${targets.joinToString { it.id }})"
                +"modes(${buildConfiguration.toString().uppercase()})"
                +"isStatic = ${linkMode.isStatic}"
                "toolOptions" {
                    +"""freeCompilerArgs.add("-Xbinary=bundleId=gradle_test")"""
                }
                if (kotlinVersion.value.startsWith("1.8.") || kotlinVersion.value.startsWith("1.9.0")) {
                    +"""linkerOptions += "-ld64""""
                }
            }
        }
    }

    class Plugin private constructor(
        val reference: String,
        val version: String?,
        val apply: Boolean = true,
    ) {
        val application: String = listOfNotNull(
            reference,
            version?.let { """version "$it"""" },
            "apply false".takeUnless { apply },
        ).joinToString(" ")

        fun apply(apply: Boolean): Plugin = Plugin(reference, version, apply)

        companion object {
            operator fun invoke(id: String, version: String?, apply: Boolean = true) = Plugin(
                reference = """id("$id")""",
                version = version,
                apply = apply,
            )

            val skie = Plugin(id = "co.touchlab.skie", version = "1.0.0-SNAPSHOT")

            fun kotlinMultiplatform(kotlinVersion: KotlinVersion) = Plugin(reference = kotlin("multiplatform"), version = kotlinVersion.value)

            private fun kotlin(component: String) = """kotlin("$component")"""
        }
    }
}
