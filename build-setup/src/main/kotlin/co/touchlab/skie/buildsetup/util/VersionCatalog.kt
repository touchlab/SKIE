package co.touchlab.skie.buildsetup.util

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalogRoot
    get() = this.extensions.getByType<VersionCatalogsExtension>().named("libs").let { VersionCatalogRoot(it) }

internal class VersionCatalogRoot(private val versionCatalog: VersionCatalog) {

    val versions: VersionCatalogVersions
        get() = VersionCatalogVersions(versionCatalog)

    val bundles: VersionCatalogBundles
        get() = VersionCatalogBundles(versionCatalog)
}

internal class VersionCatalogVersions(versionCatalog: VersionCatalog) {

    val jvmTarget = versionCatalog.findVersion("jvmTarget").get().requiredVersion.toInt()
    val jvmToolchain = versionCatalog.findVersion("jvmToolchain").get().requiredVersion.toInt()
}

internal class VersionCatalogBundles(private val versionCatalog: VersionCatalog) {

    val testing = Testing()

    inner class Testing {

        val jvm = versionCatalog.findBundle("testing-jvm").get()
    }
}
