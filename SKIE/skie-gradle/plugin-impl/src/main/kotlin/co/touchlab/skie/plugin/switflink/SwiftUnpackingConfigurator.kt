package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.skieBuildDirectory
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import co.touchlab.skie.util.file.isKlib

object SwiftUnpackingConfigurator {

    fun configureCustomSwiftUnpacking(target: SkieTarget) {
        val unpackTask = target.registerSkieTargetBasedTask<UnpackSwiftSourcesTask>("unpackSwiftSources") {
            val linkerKlibs = target.linkerConfiguration.incoming.files.filter { it.isKlib || it.isDirectory }
            inputs.files(linkerKlibs)
            dependencies.addAll(linkerKlibs)

            if (target is SkieTarget.Binary) {
                val currentModuleKlib = target.compilationProvider.flatMap { it.compileTaskOutputFileProvider }
                dependencies.add(currentModuleKlib)

                // Needed because the klib is a plain Java file
                dependsOn(target.compilationProvider.flatMap { it.compileTaskProvider })
            }

            val bundledSwiftDirectory = target.skieBuildDirectory.map { it.swift.bundled.directory }
            output.set(bundledSwiftDirectory)
        }

        val unpackTaskOutput = unpackTask.flatMap { it.output }

        target.task.configure {
            inputs.files(unpackTaskOutput)
        }
    }
}
