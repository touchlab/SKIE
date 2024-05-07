package co.touchlab.skie.plugin.shim

import co.touchlab.skie.plugin.ActualSkieArtifactTarget
import co.touchlab.skie.plugin.ActualSkieBinaryTarget
import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.util.appleTargets
import co.touchlab.skie.plugin.util.kotlinMultiplatformExtension
import co.touchlab.skie.plugin.util.named
import co.touchlab.skie.plugin.util.withType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.kotlinArtifactsExtension
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.util.Properties

// Constructed through reflection in SKIE Gradle Plugin.
@Suppress("unused")
class ActualKgpShim(
    private val project: Project,
) : KgpShim {

    override val launchScheduler = LaunchScheduler()

    override val hostIsMac: Boolean = HostManager.hostIsMac

    override fun getDistributionProperties(konanHome: String, propertyOverrides: Map<String, String>?): Properties =
        Distribution(konanHome = konanHome, propertyOverrides = propertyOverrides).properties

    override fun getKonanHome(): File =
        NativeCompilerDownloader(project).compilerDirectory

    override fun getKotlinPluginVersion(): String =
        project.getKotlinPluginVersion()

    override val targets: NamedDomainObjectContainer<SkieTarget> = project.objects.domainObjectContainer(SkieTarget::class.java)

    override fun initializeSkieTargets() {
        initializeBinaryTargets()
        initializeArtifactTargets()
    }

    private fun initializeBinaryTargets() {
        project.kotlinMultiplatformExtension?.appleTargets?.configureEach {
            val target = this

            binaries.withType<Framework>().configureEach {
                val binary = this

                val binaryTarget = ActualSkieBinaryTarget(
                    project = project,
                    target = target,
                    binary = binary,
                    outputKind = SkieTarget.OutputKind.Framework,
                )

                targets.add(binaryTarget)
            }
        }
    }

    private fun initializeArtifactTargets() {
        project.kotlinArtifactsExtension.artifacts.withType<KotlinNativeArtifact>().configureEach {
            val artifactTargets = ActualSkieArtifactTarget.createFromArtifact(this, project)

            targets.addAll(artifactTargets)
        }
    }

    override fun resolvablePropertyString(properties: Properties, key: String, suffix: String?): String? =
        properties.resolvablePropertyString(key, suffix)

    override fun addKmpAttributes(attributeContainer: AttributeContainer, konanTarget: KonanTargetShim) {
        attributeContainer.apply {
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
            attribute(KotlinNativeTarget.konanTargetAttribute, konanTarget.name)
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(KotlinUsages.KOTLIN_API))
        }
    }
}
