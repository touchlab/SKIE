package co.touchlab.skie.gradle.util

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = this.extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val VersionCatalog.versions: VersionCatalogVersions
    get() = VersionCatalogVersions(this)

internal val VersionCatalog.bundles: VersionCatalogBundles
    get() = VersionCatalogBundles(this)

internal class VersionCatalogVersions(private val versionCatalog: VersionCatalog) {

    val java = versionCatalog.findVersion("java").get().requiredVersion.toInt()
}

internal class VersionCatalogBundles(private val versionCatalog: VersionCatalog) {

    val testing = Testing()

    inner class Testing {

        val jvm = versionCatalog.findBundle("testing-jvm").get()
    }
}
