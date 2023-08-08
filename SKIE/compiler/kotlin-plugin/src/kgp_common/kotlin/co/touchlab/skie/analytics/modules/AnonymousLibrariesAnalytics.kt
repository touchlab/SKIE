@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.analytics.modules

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.UserVisibleIrModulesSupportReflector
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.ir.backend.js.moduleName
import org.jetbrains.kotlin.utils.ResolvedDependency

object AnonymousLibrariesAnalytics {

    @Serializable
    data class Library(
        val name: String,
        val version: String,
    )

    class Producer(
        private val config: KonanConfig,
    ) : AnalyticsProducer {

        override val name: String = "anonymous-libraries"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Anonymous_Libraries

        override fun produce(): String {
            val externalLibraries = config.externalLibraries.map { library ->
                Library(
                    name = library.canonicalName,
                    version = library.selectedVersion.version,
                )
            }

            return externalLibraries.toPrettyJson()
        }
    }
}

val KonanConfig.externalLibraries: Collection<ResolvedDependency>
    get() = userVisibleIrModulesSupport
        .reflectedBy<UserVisibleIrModulesSupportReflector>()
        .externalDependencyModules

val ResolvedDependency.canonicalName: String
    get() = id.uniqueNames.minBy { it.length }
