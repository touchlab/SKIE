package co.touchlab.skie.api.phases

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.util.cache.copyFileToIfDifferent

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
        return framework.kotlinHeader.copyFileToIfDifferent(dummyFramework.kotlinHeader) or
            framework.modulemapFile.copyFileToIfDifferent(dummyFramework.modulemapFile) or
            framework.apiNotes.copyFileToIfDifferent(dummyFramework.apiNotes)
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
