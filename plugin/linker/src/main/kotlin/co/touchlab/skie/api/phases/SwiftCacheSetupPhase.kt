package co.touchlab.skie.api.phases

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

class SwiftCacheSetupPhase(
    private val skieContext: SkieContext,
    private val framework: FrameworkLayout,
) : SkieLinkingPhase {

    override fun execute() {
        val wasChanged = synchronizeDummyKotlinFramework()

        if (wasChanged) {
            deleteKotlinFrameworkCache()
        }
    }

    private fun synchronizeDummyKotlinFramework(): Boolean {
        val dummyFramework = skieContext.cacheableKotlinFramework

        dummyFramework.headersDir.mkdirs()
        dummyFramework.modulesDir.mkdirs()

        if (!dummyFramework.swiftHeader.exists()) {
            dummyFramework.swiftHeader.writeText("")
        }

        // Must use `or` to prevent short circuit optimization.
        return syncIfDifferent(framework.kotlinHeader, dummyFramework.kotlinHeader) or
            syncIfDifferent(framework.modulemapFile, dummyFramework.modulemapFile) or
            syncIfDifferent(framework.apiNotes, dummyFramework.apiNotes)
    }

    private fun syncIfDifferent(source: File, destination: File): Boolean {
        val sourceContent = source.readText()
        val destinationContent = if (destination.exists()) destination.readText() else null

        if (sourceContent != destinationContent) {
            destination.writeText(sourceContent)

            return true
        }

        return false
    }

    // Solves a bug in Swift compiler.
    // If the module cache is not deleted then all threads rebuild the same cache in series (the caching is done in a synchronized block).
    // This could lead to significant performance degradation if the Obj-C takes a long time to load.
    private fun deleteKotlinFrameworkCache() {
        skieContext.skieBuildDirectory.cache.swiftModules.directory.walkTopDown()
            .filter { it.isFile && it.extension == "pcm" && it.name.startsWith(framework.moduleName + "-") }
            .forEach {
                it.delete()
            }
    }
}

val SkieContext.cacheableKotlinFramework: FrameworkLayout
    get() = FrameworkLayout(skieBuildDirectory.cache.cacheableKotlinFramework.framework(frameworkLayout.moduleName))




