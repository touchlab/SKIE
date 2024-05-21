package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.skieBuildDirectory
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import co.touchlab.skie.util.file.isKlib

object SwiftUnpackingConfigurator {

    fun configureCustomSwiftUnpacking(target: SkieTarget) {
        val unpackTask = target.registerSkieTargetBasedTask<UnpackSwiftSourcesTask>("unpackSwiftSources") {
            val linkerKlibs = target.linkerConfiguration.fileCollection { true }.filter { it.isKlib }
            klibs.from(linkerKlibs)

            if (target is SkieTarget.Binary) {
                val currentModuleKlib = target.compilationProvider.map { it.compileTaskOutputFileProvider }

                klibs.from(currentModuleKlib)
                // Needed because the klib is a plain Java file
                dependsOn(target.compilationProvider.flatMap { it.compileTaskProvider })
            }

            val bundledSwiftDirectory = target.skieBuildDirectory.map { it.swift.bundled.directory }
            output.set(bundledSwiftDirectory)
        }

        target.task.configure {
            inputs.files(unpackTask.map { it.output })
        }
    }
}
