import org.gradle.tooling.GradleConnector

plugins {
    kotlin("jvm") version "1.9.20"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

val smokeTestRepository = layout.buildDirectory.dir("smokeTestRepo")

val publishSkieToTempMaven by tasks.registering {
    doLast {
        GradleConnector.newConnector()
            .forProjectDirectory(rootDir.resolve("../SKIE"))
            .connect()
            .use { projectConnection ->
                projectConnection.newBuild()
                    .forTasks("publishAllPublicationsToSmokeTestTmpRepositoryRepository")
                    .setStandardInput(System.`in`)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .addArguments("-PsmokeTestTmpRepositoryPath=${smokeTestRepository.get().asFile.absolutePath}")
                    .run()
            }
    }
}

tasks.test {
    useJUnitPlatform()

    dependsOn(publishSkieToTempMaven)

    systemProperty("smokeTestRepository", smokeTestRepository.get().asFile.absolutePath)
}
