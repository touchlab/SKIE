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
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkTask
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.kotlinArtifactsExtension
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
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

    override val skieTargets: NamedDomainObjectContainer<SkieTarget> = project.objects.domainObjectContainer(SkieTarget::class.java)

    override val kotlinNativeTargets: NamedDomainObjectContainer<KotlinNativeTargetShim> =
        project.objects.domainObjectContainer(KotlinNativeTargetShim::class.java)

    override fun initializeShim() {
        initializeKotlinNativeTargets()
        initializeBinaryTargets()
        initializeArtifactTargets()
    }

    private fun initializeKotlinNativeTargets() {
        project.kotlinMultiplatformExtension?.appleTargets?.configureEach {
            val shim = ActualKotlinNativeTargetShim(this, project.objects)

            kotlinNativeTargets.add(shim)
        }
    }

    private fun initializeBinaryTargets() {
        val frameworksUsedInXCFrameworks = getFrameworksUsedInXCFrameworks()

        project.kotlinMultiplatformExtension?.appleTargets?.configureEach {
            val target = this

            binaries.withType<Framework>().configureEach {
                registerBinaryTarget(target, this, frameworksUsedInXCFrameworks)
            }
        }
    }

    // Must be done eagerly because we need to go through all potential tasks even those that will not be executed.
    // This is to ensure the Link task configuration will be the same when executed separately or as part of the XCFramework task.
    private fun getFrameworksUsedInXCFrameworks(): Set<Framework> =
        project.tasks.withType<XCFrameworkTask>()
            // Prevents concurrent modification exception.
            .toList()
            .flatMap { xcFrameworkTask ->
                xcFrameworkTask.taskDependencies.getDependencies(xcFrameworkTask).filterIsInstance<KotlinNativeLink>().map { it.binary }
            }
            .filterIsInstance<Framework>()
            .toSet()

    private fun registerBinaryTarget(target: KotlinNativeTarget, binary: Framework, frameworksUsedInXCFrameworks: Set<Framework>) {
        val binaryTarget = ActualSkieBinaryTarget(
            project = project,
            target = target,
            binary = binary,
            isForXCFramework = binary in frameworksUsedInXCFrameworks,
        )

        skieTargets.add(binaryTarget)
    }

    private fun initializeArtifactTargets() {
        project.kotlinArtifactsExtension.artifacts.withType<KotlinNativeArtifact>().configureEach {
            val artifactTargets = ActualSkieArtifactTarget.createFromArtifact(this, project)

            skieTargets.addAll(artifactTargets)
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

    override fun configureEachFatFrameworkTask(action: FatFrameworkTaskShim.() -> Unit) {
        project.tasks.withType<FatFrameworkTask>().configureEach {
            ActualFatFrameworkTaskShim(this).action()
        }
    }
}
