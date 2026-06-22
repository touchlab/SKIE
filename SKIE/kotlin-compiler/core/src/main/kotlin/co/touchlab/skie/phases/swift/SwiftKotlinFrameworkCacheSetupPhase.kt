package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.cache.copyFileToIfDifferent
import co.touchlab.skie.util.directory.FrameworkLayout

object SwiftKotlinFrameworkCacheSetupPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        val wasChanged = synchronizeDummyKotlinFramework()

        if (wasChanged) {
            deleteKotlinFrameworkCache()
        }
    }

    context(context: SirPhase.Context)
    private fun synchronizeDummyKotlinFramework(): Boolean {
        val dummyFramework = context.cacheableKotlinFramework

        // Must use `or` to prevent short circuit optimization.
        return context.framework.kotlinHeader.copyFileToIfDifferent(dummyFramework.kotlinHeader) or
            context.framework.modulemapFile.copyFileToIfDifferent(dummyFramework.modulemapFile) or
            context.skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(context.framework.frameworkName)
                .copyFileToIfDifferent(dummyFramework.apiNotes)
    }

    // Solves a bug in Swift compiler.
    // If the module cache is not deleted then all threads rebuild the same cache in series (the caching is done in a synchronized block).
    // This could lead to significant performance degradation if the Obj-C takes a long time to load.
    context(context: SirPhase.Context)
    private fun deleteKotlinFrameworkCache() {
        context.skieBuildDirectory.cache.swiftModules.directory.walkTopDown()
            .filter { it.isFile && it.extension == "pcm" && it.name.startsWith(context.framework.frameworkName + "-") }
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
