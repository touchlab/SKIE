package co.touchlab.skie.gradle.util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.util.*

internal val Project.libs: VersionCatalogRoot
    get() = this.extensions.getByType<VersionCatalogsExtension>().named("libs").let { VersionCatalogRoot(it) }

internal class VersionCatalogRoot(private val versionCatalog: VersionCatalog) {

    val versions: VersionCatalogVersions
        get() = VersionCatalogVersions(versionCatalog)

    val bundles: VersionCatalogBundles
        get() = VersionCatalogBundles(versionCatalog)
}

internal class VersionCatalogVersions(private val versionCatalog: VersionCatalog) {

    val java = versionCatalog.findVersion("java").get().requiredVersion.toInt()
}

internal class VersionCatalogBundles(private val versionCatalog: VersionCatalog) {

    val testing = Testing()

    inner class Testing {

        val jvm = versionCatalog.findBundle("testing-jvm").get()
    }
}
