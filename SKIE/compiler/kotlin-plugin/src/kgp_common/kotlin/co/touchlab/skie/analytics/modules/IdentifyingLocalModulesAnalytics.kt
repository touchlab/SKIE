@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.analytics.modules

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.hashed
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.ir.backend.js.moduleName
import org.jetbrains.kotlin.library.KotlinLibrary

object IdentifyingLocalModulesAnalytics {

    @Serializable
    data class Module(
        val id: String,
        val name: String,
    )

    class Producer(
        private val config: KonanConfig,
    ) : AnalyticsProducer {

        override val name: String = "identifying-local-modules"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Identifying_LocalModules

        override fun produce(): String =
            config.localModules.map { Module(it.localModuleId, it.moduleName) }.toPrettyJson()
    }
}

val KonanConfig.localModules: Collection<KotlinLibrary>
    get() {
        val modulesWithoutBuiltIns = resolvedLibraries.getFullList().filter { !it.isDefault }

        val externalLibrariesArtifacts = externalLibraries.flatMap { it.artifactPaths }.map { it.path }.toSet()

        return modulesWithoutBuiltIns.filter { it.libraryFile.absolutePath !in externalLibrariesArtifacts }
    }

val KotlinLibrary.localModuleId: String
    get() = moduleName.hashed()
