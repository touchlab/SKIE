@file:Suppress("MemberVisibilityCanBePrivate")

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.util.takeIf
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class SkieDebugConfiguration @Inject constructor(objects: ObjectFactory) {

    val verifyDescriptorProviderConsistency: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val dumpSwiftApiBeforeApiNotes: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val dumpSwiftApiAfterApiNotes: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val printSkiePerformanceLogs: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val crashOnSoftErrors: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val loadAllPlatformApiNotes: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val generateFileForEachExportedClass: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val useStableTypeAliases: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> = setOfNotNull(
        SkieConfigurationFlag.Debug_VerifyDescriptorProviderConsistency takeIf verifyDescriptorProviderConsistency,
        SkieConfigurationFlag.Debug_DumpSwiftApiBeforeApiNotes takeIf dumpSwiftApiBeforeApiNotes,
        SkieConfigurationFlag.Debug_DumpSwiftApiAfterApiNotes takeIf dumpSwiftApiAfterApiNotes,
        SkieConfigurationFlag.Debug_PrintSkiePerformanceLogs takeIf printSkiePerformanceLogs,
        SkieConfigurationFlag.Debug_CrashOnSoftErrors takeIf crashOnSoftErrors,
        SkieConfigurationFlag.Debug_LoadAllPlatformApiNotes takeIf loadAllPlatformApiNotes,
        SkieConfigurationFlag.Debug_GenerateFileForEachExportedClass takeIf generateFileForEachExportedClass,
        SkieConfigurationFlag.Debug_UseStableTypeAliases takeIf useStableTypeAliases,
    )
}
