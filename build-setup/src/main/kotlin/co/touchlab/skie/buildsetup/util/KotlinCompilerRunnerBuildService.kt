package co.touchlab.skie.buildsetup.util

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class KotlinCompilerRunnerBuildService : BuildService<KotlinCompilerRunnerBuildService.Parameters> {

    interface Parameters : BuildServiceParameters {

        val classpath: ConfigurableFileCollection
    }

    private val runner = KotlinCompilerRunner(parameters.classpath.files)

    fun compile(compilerClassFqName: String, arguments: Array<String>) {
        runner.compile(compilerClassFqName, arguments)
    }
}
