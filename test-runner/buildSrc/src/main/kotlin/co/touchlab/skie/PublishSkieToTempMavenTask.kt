package co.touchlab.skie

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import javax.inject.Inject

abstract class PublishSkieToTempMavenTask @Inject constructor(objects: ObjectFactory): DefaultTask() {
    // This is @Internal because we only want `.kt` files to be considered as inputs, which we do in `init { }`
    @get:Internal
    val skieSources: DirectoryProperty = objects.directoryProperty()

    @get:OutputDirectory
    val tempRepository: DirectoryProperty = objects.directoryProperty()

    init {
        inputs.files(skieSources.map { it.asFileTree.matching { this.include { it.file.extension == "kt" } }.also { println(it.files.joinToString("\n") { it.absolutePath }) } })
    }

    @TaskAction
    fun publish() {
        GradleConnector.newConnector()
            .forProjectDirectory(skieSources.get().asFile)
            .connect()
            .use { projectConnection ->
                projectConnection.newBuild()
                    .forTasks("publishAllPublicationsToSmokeTestTmpRepository")
                    .setStandardInput(System.`in`)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .addArguments("-PsmokeTestTmpRepositoryPath=${tempRepository.get().asFile.absolutePath}")
                    .run()
            }
    }
}
