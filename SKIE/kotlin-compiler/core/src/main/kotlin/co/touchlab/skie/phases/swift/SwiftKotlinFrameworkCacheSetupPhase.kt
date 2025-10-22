package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.cache.copyFileToIfDifferent
import co.touchlab.skie.util.directory.FrameworkLayout

object SwiftKotlinFrameworkCacheSetupPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val wasChanged = synchronizeDummyKotlinFramework()

        if (wasChanged) {
            deleteKotlinFrameworkCache()
        }
    }

    context(SirPhase.Context)
    private fun synchronizeDummyKotlinFramework(): Boolean {
        val dummyFramework = cacheableKotlinFramework

        // Must use `or` to prevent short circuit optimization.
        return framework.kotlinHeader.copyFileToIfDifferent(dummyFramework.kotlinHeader) or
            framework.modulemapFile.copyFileToIfDifferent(dummyFramework.modulemapFile) or
            skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(framework.frameworkName)
                .copyFileToIfDifferent(dummyFramework.apiNotes)
    }

    // Solves a bug in Swift compiler.
    // If the module cache is not deleted then all threads rebuild the same cache in series (the caching is done in a synchronized block).
    // This could lead to significant performance degradation if the Obj-C takes a long time to load.
    context(SirPhase.Context)
    private fun deleteKotlinFrameworkCache() {
        skieBuildDirectory.cache.swiftModules.directory.walkTopDown()
            .filter { it.isFile && it.extension == "pcm" && it.name.startsWith(framework.frameworkName + "-") }
            .forEach {
                it.delete()
            }
    }
}

val SirPhase.Context.cacheableKotlinFramework: FrameworkLayout
    get() = FrameworkLayout(
        frameworkDirectory = skieBuildDirectory.cache.cacheableKotlinFramework.framework(framework.frameworkName),
        isMacosFramework = swiftCompilerConfiguration.targetTriple.isMacos,
        isSkieCache = true,
    )
